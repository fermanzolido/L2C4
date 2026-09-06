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
package org.l2jmobius.loginserver.network.loginserverpackets;

import org.l2jmobius.commons.enums.PasswordChangeResult;
import org.l2jmobius.loginserver.network.AbstractServerPacket;

/**
 * The answer to a {@code ChangePassword} request, addressed to the character that asked for it.
 * <p>
 * The password itself never travels back: the game server has no use for it, and a value that is
 * not sent cannot be logged by accident on the way.
 * @author Mobius
 */
public class ChangePasswordResponse extends AbstractServerPacket
{
	public ChangePasswordResponse(String characterName, PasswordChangeResult result)
	{
		writeByte(0x05);
		writeString(characterName);
		writeByte(result.ordinal());
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
