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
package org.l2jmobius.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.loginserver.GameServerTable.GameServer;
import org.l2jmobius.loginserver.network.AbstractServerPacket;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;

/**
 * ServerList Format: cc [cddcchhcdc] c: server list size (number of servers) c: ? [ (repeat for each servers) c: server id (ignored by client?) d: server ip d: server port c: age limit (used by client?) c: pvp or not (used by client?) h: current number of players h: max number of players c: 0 if
 * server is down d: 2nd bit: clock 3rd bit: wont dsiplay server name 4th bit: test server (used by client?) c: 0 if you dont want to display brackets in front of sever name ] Server will be considered as Good when the number of online players is less than half the maximum. as Normal between half
 * and 4/5 and Full when there's more than 4/5 of the maximum number of players
 */
public class ServerList extends AbstractServerPacket
{
	private final List<ServerData> _servers;
	
	private boolean _listDone = false;
	private final int _lastServer;
	
	class ServerData
	{
		protected String _ip;
		protected int _port;
		protected boolean _pvp;
		protected int _currentPlayers;
		protected int _maxPlayers;
		protected boolean _testServer;
		protected boolean _brackets;
		protected boolean _clock;
		protected int _status;
		protected int _serverId;
		
		ServerData(String pIp, GameServer gs, int pStatus)
		{
			_ip = pIp;
			_port = gs.port;
			_pvp = gs.pvp;
			_testServer = gs.testServer;
			_currentPlayers = (gs.gst == null ? 0 : gs.gst.getCurrentPlayers());
			_maxPlayers = gs.maxPlayers;
			_brackets = gs.brackets;
			_clock = gs.clock;
			_status = pStatus;
			_serverId = gs.serverId;
		}
	}
	
	public ServerList(int lastServer)
	{
		_lastServer = lastServer;
		_servers = new ArrayList<>();
	}
	
	public void addServer(String ip, GameServer game, int status)
	{
		_servers.add(new ServerData(ip, game, status));
	}
	
	@Override
	public byte[] getContent()
	{
		if (!_listDone) // list should only be done once even if there are multiple getContent calls
		{
			writeByte(0x04);
			writeByte(_servers.size());
			writeByte(_lastServer);
			
			for (ServerData server : _servers)
			{
				writeByte(server._serverId); // server id
				try
				{
					final InetAddress i4 = InetAddress.getByName(server._ip);
					final byte[] raw = i4.getAddress();
					writeByte(raw[0] & 0xff);
					writeByte(raw[1] & 0xff);
					writeByte(raw[2] & 0xff);
					writeByte(raw[3] & 0xff);
				}
				catch (UnknownHostException e)
				{
					e.printStackTrace();
					writeByte(127);
					writeByte(0);
					writeByte(0);
					writeByte(1);
				}
				
				writeInt(server._port);
				writeByte(0x00); // age limit
				writeByte(server._pvp ? 0x01 : 0x00);
				
				writeShort(server._currentPlayers);
				writeShort(server._maxPlayers);
				writeByte(server._status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
				
				int bits = 0;
				if (server._testServer)
				{
					bits |= 0x04;
				}
				
				if (server._clock)
				{
					bits |= 0x02;
				}
				
				writeInt(bits);
				writeByte(server._brackets ? 0x01 : 0x00);
			}
			
			_listDone = true;
		}
		
		return getBytes();
	}
}
