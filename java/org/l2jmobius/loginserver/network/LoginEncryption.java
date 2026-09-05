/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.loginserver.network;

import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.commons.util.Rnd;

/**
 * Cipher for the login client connection.
 * <p>
 * The first packet out of the server, Init, is the only one the client cannot decrypt with a key it
 * was given, because it is the packet that carries that key. So it goes out obfuscated with an XOR
 * pass and enciphered under a fixed key both sides already know. Every packet after it carries a
 * checksum and uses the per-connection key Init delivered.
 * @author KenM
 */
public class LoginEncryption
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};

	private static final NewCrypt STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);

	private NewCrypt _crypt = null;
	private boolean _static = true;

	/**
	 * Initializes the cipher with the per-connection key that Init hands to the client.
	 * @param key the blowfish key used for every packet after Init
	 */
	public void setKey(byte[] key)
	{
		_crypt = new NewCrypt(key);
	}

	/**
	 * @param length the plain content length
	 * @return the buffer size that {@link #encrypt} will fill, checksum, XOR key and padding included
	 */
	public int getEncryptedSize(int length)
	{
		// reserve checksum
		int size = length + 4;

		if (_static)
		{
			// reserve for XOR "key"
			size += 4;
		}

		// padding
		size += 8 - (size % 8);
		return size;
	}

	/**
	 * Enciphers an outgoing packet in place. The buffer must be at least {@link #getEncryptedSize}
	 * bytes long, since padding and the checksum are written past the plain content.
	 * @param raw array holding the plain data, sized for the result
	 * @param offset offset where the plain data starts
	 * @param length number of bytes of plain data
	 */
	public void encrypt(byte[] raw, int offset, int length)
	{
		final int size = getEncryptedSize(length);
		if (_static)
		{
			NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
			STATIC_CRYPT.crypt(raw, offset, size);
			_static = false;
		}
		else
		{
			NewCrypt.appendChecksum(raw, offset, size);
			_crypt.crypt(raw, offset, size);
		}
	}

	/**
	 * Deciphers an incoming packet in place. Only the per-connection key is ever used here: the
	 * client has it from Init before it sends anything.
	 * @param raw array with enciphered data
	 * @param offset offset where the enciphered data starts
	 * @param size number of bytes of enciphered data
	 */
	public void decrypt(byte[] raw, int offset, int size)
	{
		_crypt.decrypt(raw, offset, size);
	}
}
