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
package org.l2jmobius.gameserver.network;

import org.l2jmobius.commons.network.Buffer;

/**
 * @author KenM
 */
public class Encryption
{
	private byte[] _inKey;
	private byte[] _outKey;
	private int _keyMask;
	private int _shiftOffset;
	private boolean _isEnabled;
	
	/**
	 * Takes the key length from the key itself, because the two chronicles differ on it: C4 cycles
	 * the cipher over eight bytes and rolls its counter at the front, Interlude cycles over sixteen
	 * -- eight sent to the client plus a tail it already knows -- and rolls the counter at byte 8.
	 * @param key the key handed to the client in KeyPacket, 8 or 16 bytes long
	 */
	public void setKey(byte[] key)
	{
		_inKey = key.clone();
		_outKey = key.clone();
		_keyMask = key.length - 1;
		_shiftOffset = key.length == 16 ? 8 : 0;
	}
	
	public void encrypt(Buffer data, int offset, int size)
	{
		if (!_isEnabled)
		{
			_isEnabled = true;
			return;
		}
		
		int encrypted = 0;
		for (int i = 0; i < size; i++)
		{
			final int raw = data.readByte(offset + i);
			encrypted = raw ^ _outKey[i & _keyMask] ^ encrypted;
			data.writeByte(offset + i, (byte) encrypted);
		}
		
		// Shift key.
		int old = _outKey[_shiftOffset + 0] & 0xff;
		old |= (_outKey[_shiftOffset + 1] << 8) & 0xff00;
		old |= (_outKey[_shiftOffset + 2] << 16) & 0xff0000;
		old |= (_outKey[_shiftOffset + 3] << 24) & 0xff000000;
		old += size;
		_outKey[_shiftOffset + 0] = (byte) (old & 0xff);
		_outKey[_shiftOffset + 1] = (byte) ((old >> 8) & 0xff);
		_outKey[_shiftOffset + 2] = (byte) ((old >> 16) & 0xff);
		_outKey[_shiftOffset + 3] = (byte) ((old >> 24) & 0xff);
	}
	
	public void decrypt(Buffer data, int offset, int size)
	{
		if (!_isEnabled)
		{
			return;
		}
		
		int xOr = 0;
		for (int i = 0; i < size; i++)
		{
			final int encrypted = data.readByte(offset + i);
			data.writeByte(offset + i, (byte) (encrypted ^ _inKey[i & _keyMask] ^ xOr));
			xOr = encrypted;
		}
		
		// Shift key.
		int old = _inKey[_shiftOffset + 0] & 0xff;
		old |= (_inKey[_shiftOffset + 1] << 8) & 0xff00;
		old |= (_inKey[_shiftOffset + 2] << 16) & 0xff0000;
		old |= (_inKey[_shiftOffset + 3] << 24) & 0xff000000;
		old += size;
		_inKey[_shiftOffset + 0] = (byte) (old & 0xff);
		_inKey[_shiftOffset + 1] = (byte) ((old >> 8) & 0xff);
		_inKey[_shiftOffset + 2] = (byte) ((old >> 16) & 0xff);
		_inKey[_shiftOffset + 3] = (byte) ((old >> 24) & 0xff);
	}
}
