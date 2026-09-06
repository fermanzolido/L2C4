/*
 * Copyright (c) 2013 L2jMobius
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.gameserver.network.loginserverpackets.login;

import org.l2jmobius.commons.enums.PasswordChangeResult;
import org.l2jmobius.commons.network.base.BaseReadablePacket;

/**
 * The login server's verdict on a password change, addressed to the character that asked.
 * @author Mobius
 */
public class ChangePasswordResponse extends BaseReadablePacket
{
	private final String _characterName;
	private final PasswordChangeResult _result;

	public ChangePasswordResponse(byte[] decrypt)
	{
		super(decrypt);

		readByte(); // Packet id, it is already processed.
		_characterName = readString();

		// A login server newer than this game server could name an outcome this one has never
		// heard of. Report that as a failure rather than throwing on the connection thread.
		final int ordinal = readByte();
		final PasswordChangeResult[] values = PasswordChangeResult.values();
		_result = (ordinal >= 0) && (ordinal < values.length) ? values[ordinal] : PasswordChangeResult.DATABASE_ERROR;
	}

	public String getCharacterName()
	{
		return _characterName;
	}

	public PasswordChangeResult getResult()
	{
		return _result;
	}
}
