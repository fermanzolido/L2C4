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
package org.l2jmobius.loginserver.network.gameserverpackets;

import org.l2jmobius.loginserver.network.AbstractGameServerPacket;

/**
 * A game server asking for an account's password to be changed, on behalf of a player who typed
 * the voiced command in game. The field order matches the game server's writer of the same name.
 * <p>
 * The character name is carried so the answer can be addressed back to whoever asked: the login
 * server knows the account, but only the game server knows which of its characters is standing
 * there waiting to be told whether it worked.
 * @author Mobius
 */
public class ChangePassword extends AbstractGameServerPacket
{
	private final String _account;
	private final String _characterName;
	private final String _currentPassword;
	private final String _newPassword;

	public ChangePassword(byte[] decrypt)
	{
		super(decrypt);

		_account = readString();
		_characterName = readString();
		_currentPassword = readString();
		_newPassword = readString();
	}

	public String getAccount()
	{
		return _account;
	}

	public String getCharacterName()
	{
		return _characterName;
	}

	public String getCurrentPassword()
	{
		return _currentPassword;
	}

	public String getNewPassword()
	{
		return _newPassword;
	}
}
