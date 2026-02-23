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
package org.l2jmobius.loginserver.network.clientpackets;

import org.l2jmobius.loginserver.GameServerTable;
import org.l2jmobius.loginserver.network.AbstractClientPacket;
import org.l2jmobius.loginserver.network.LoginClient;
import org.l2jmobius.loginserver.network.serverpackets.LoginFail;

/**
 * Format: ddc d: fist part of session id d: second part of session id c: ? (session ID is sent in LoginOk packet and fixed to 0x55555555 0x44444444)
 */
public class RequestServerList extends AbstractClientPacket
{
	private final int _key1;
	private final int _key2;
	private final int _data3;
	
	public int getKey1()
	{
		return _key1;
	}
	
	public int getKey2()
	{
		return _key2;
	}
	
	public int getData3()
	{
		return _data3;
	}
	
	public RequestServerList(byte[] rawPacket, LoginClient client)
	{
		super(rawPacket, client);
		_key1 = readInt(); // loginOk 1
		_key2 = readInt(); // loginOk 2
		_data3 = readByte(); // ?
	}
	
	@Override
	public void run()
	{
		if ((getClient().getSessionKey() != null) && getClient().getSessionKey().checkLoginPair(_key1, _key2))
		{
			getClient().setHasAgreed(true);
			GameServerTable.getInstance().createServerList(getClient());
			
		}
		else
		{
			getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
		}
	}
}
