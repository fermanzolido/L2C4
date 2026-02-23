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
import org.l2jmobius.loginserver.LoginController;
import org.l2jmobius.loginserver.config.LoginConfig;
import org.l2jmobius.loginserver.network.AbstractClientPacket;
import org.l2jmobius.loginserver.network.LoginClient;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.loginserver.network.serverpackets.LoginFail;
import org.l2jmobius.loginserver.network.serverpackets.PlayFail;
import org.l2jmobius.loginserver.network.serverpackets.PlayOk;

/**
 * Fromat is ddc d: first part of session id d: second part of session id c: server ID (session ID is sent in LoginOk packet and fixed to 0x55555555 0x44444444)
 */
public class RequestServerLogin extends AbstractClientPacket
{
	// private static final Logger LOGGER = Logger.getLogger(RequestServerLogin.class.getName());
	
	private final int _key1;
	private final int _key2;
	private final int _server_id;
	
	public int getKey1()
	{
		return _key1;
	}
	
	public int getKey2()
	{
		return _key2;
	}
	
	public int getServerID()
	{
		return _server_id;
	}
	
	public RequestServerLogin(byte[] rawPacket, LoginClient client)
	{
		super(rawPacket, client);
		_key1 = readInt();
		_key2 = readInt();
		
		_server_id = readByte(); // = rawPacket[9] &0xff;
	}
	
	@Override
	public void run()
	{
		
		final LoginController lc = LoginController.getInstance();
		final int status = GameServerTable.getInstance().getGameServerStatus(getServerID());
		if ((status == ServerStatus.STATUS_DOWN) || ((status == ServerStatus.STATUS_GM_ONLY) && (getClient().getAccessLevel() <= 0)))
		{
			getClient().sendPacket(new PlayFail(PlayFail.REASON_SYSTEM_ERROR));
			return;
		}
		
		final int onlinePlayers = lc.getOnlinePlayerCount(getServerID());
		if (onlinePlayers >= lc.getMaxAllowedOnlinePlayers(getServerID()))
		{
			if (onlinePlayers == 0)
			{
				getClient().sendPacket(new PlayFail(PlayFail.REASON_SYSTEM_ERROR));
				return;
			}
			
			if (getClient().getAccessLevel() <= 0)
			{
				getClient().sendPacket(new PlayFail(PlayFail.REASON_TOO_MANY_PLAYERS));
				return;
			}
		}
		
		if (LoginConfig.SHOW_LICENCE)
		{
			if (!getClient().getSessionKey().checkLoginPair(_key1, _key2))
			{
				getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
				return;
			}
			
			// if (LoginConfig.L2WALKER_PROTECTION && !getClient().hasAgreed())
			// {
			// LOGGER.warning("Account " + getClient().getAccount() + " tried to log in using a 3rd party program.");
			// getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
			// return;
			// }
			
			getClient().setHasAgreed(false);
		}
		
		if (getClient().getLastServer() != getServerID())
		{
			lc.saveLastServer(getClient().getAccount(), getServerID());
		}
		
		getClient().sendPacket(new PlayOk(getClient().getSessionKey()));
		getClient().setAccount(null);
	}
}
