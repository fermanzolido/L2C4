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
package org.l2jmobius.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.commons.network.base.BaseWritablePacket;
import org.l2jmobius.commons.util.HexUtil;
import org.l2jmobius.commons.util.TraceUtil;
import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.config.ServerConfig;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.loginserverpackets.game.AuthRequest;
import org.l2jmobius.gameserver.network.loginserverpackets.game.BlowFishKey;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ChangeAccessLevel;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerAuthRequest;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerInGame;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerLogout;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ServerStatus;
import org.l2jmobius.gameserver.network.loginserverpackets.login.AuthResponse;
import org.l2jmobius.gameserver.network.loginserverpackets.login.InitLS;
import org.l2jmobius.gameserver.network.loginserverpackets.login.KickPlayer;
import org.l2jmobius.gameserver.network.loginserverpackets.login.LoginServerFail;
import org.l2jmobius.gameserver.network.loginserverpackets.login.PlayerAuthResponse;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectionInfo;
import org.l2jmobius.gameserver.network.serverpackets.LoginFail;
import org.l2jmobius.gameserver.network.serverpackets.ServerClose;

/**
 * Handles communication between the game server and login server.<br>
 * Manages player authentication, server status updates and various administrative functions.
 */
public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class.getName());
	
	// Protocol constants.
	private static final int REVISION = 0x0102; // @see org.l2jmobius.loginserver.LoginServer#PROTOCOL_REV
	private static final int RECONNECT_DELAY = 5000; // 5 seconds.
	private static final int BLOWFISH_KEY_SIZE = 40;
	private static final int HEX_ID_SIZE = 16;
	private static final int PACKET_PADDING = 8;
	
	// Connection configuration.
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private final boolean _acceptAlternate;
	private final boolean _reserveHost;
	private final List<String> _subnets;
	private final List<String> _hosts;
	
	// Connection state.
	private Socket _socket;
	private OutputStream _out;
	private NewCrypt _blowfish;
	private byte[] _hexID;
	private int _requestID;
	
	// Server state.
	private int _maxPlayer;
	private final Set<WaitingClient> _waitingClients = ConcurrentHashMap.newKeySet();
	private final Map<String, GameClient> _accountsInGameServer = new ConcurrentHashMap<>();
	private int _status;
	private String _serverName;
	
	protected LoginServerThread()
	{
		super("LoginServerThread");
		
		// Initialize connection settings.
		_port = ServerConfig.GAME_SERVER_LOGIN_PORT;
		_gamePort = ServerConfig.PORT_GAME;
		_hostname = ServerConfig.GAME_SERVER_LOGIN_HOST;
		_acceptAlternate = ServerConfig.ACCEPT_ALTERNATE_ID;
		_reserveHost = ServerConfig.RESERVE_HOST_ON_LOGIN;
		_subnets = ServerConfig.GAME_SERVER_SUBNETS;
		_hosts = ServerConfig.GAME_SERVER_HOSTS;
		_maxPlayer = ServerConfig.MAXIMUM_ONLINE_USERS;
		
		// Initialize server identification.
		_hexID = ServerConfig.HEX_ID;
		if (_hexID == null)
		{
			_requestID = ServerConfig.REQUEST_ID;
			_hexID = HexUtil.generateHexBytes(HEX_ID_SIZE);
		}
		else
		{
			_requestID = ServerConfig.SERVER_ID;
		}
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			
			try
			{
				// Establish connection to login server.
				LOGGER.info(getClass().getSimpleName() + ": Connecting to login on " + _hostname + ":" + _port);
				_socket = new Socket(_hostname, _port);
				final InputStream in = _socket.getInputStream();
				_out = new BufferedOutputStream(_socket.getOutputStream());
				
				// Initialize Blowfish encryption with default key.
				final byte[] blowfishKey = HexUtil.generateHexBytes(BLOWFISH_KEY_SIZE);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				
				while (!isInterrupted())
				{
					// Read packet length.
					lengthLo = in.read();
					lengthHi = in.read();
					length = (lengthHi * 256) + lengthLo;
					if (lengthHi < 0)
					{
						LOGGER.finer(getClass().getSimpleName() + ": Login terminated the connection.");
						break;
					}
					
					// Read packet data.
					final byte[] incoming = new byte[length - 2];
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while ((newBytes != -1) && (receivedBytes < (length - 2)))
					{
						newBytes = in.read(incoming, receivedBytes, left);
						receivedBytes += newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != (length - 2))
					{
						LOGGER.warning(getClass().getSimpleName() + ": Incomplete packet received, closing connection (LS)");
						break;
					}
					
					// Decrypt and verify packet.
					_blowfish.decrypt(incoming, 0, incoming.length);
					checksumOk = NewCrypt.verifyChecksum(incoming);
					if (!checksumOk)
					{
						LOGGER.warning(getClass().getSimpleName() + ": Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					// Process packet based on type.
					final int packetType = incoming[0] & 0xff;
					switch (packetType)
					{
						case 0x00: // InitLS - Initialize login server communication.
						{
							final InitLS init = new InitLS(incoming);
							if (init.getRevision() != REVISION)
							{
								LOGGER.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							// Generate RSA public key from login server data.
							RSAPublicKey publicKey;
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}
							catch (GeneralSecurityException e)
							{
								LOGGER.warning(getClass().getSimpleName() + ": Trouble initializing RSA public key from login server");
								break;
							}
							
							// Send encrypted blowfish key and switch to secure communication.
							sendPacket(new BlowFishKey(blowfishKey, publicKey));
							_blowfish = new NewCrypt(blowfishKey);
							sendPacket(new AuthRequest(_requestID, _acceptAlternate, _hexID, _gamePort, _reserveHost, _maxPlayer, _subnets, _hosts));
							break;
						}
						case 0x01: // LoginServerFail - Registration failure.
						{
							final LoginServerFail lsf = new LoginServerFail(incoming);
							LOGGER.info(getClass().getSimpleName() + ": Registration failed: " + lsf.getReasonString());
							break;
						}
						case 0x02: // AuthResponse - Server registration successful.
						{
							final AuthResponse aresp = new AuthResponse(incoming);
							final int serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							ServerConfig.saveHexid(serverID, hexToString(_hexID));
							LOGGER.info(getClass().getSimpleName() + ": Registered on login as Server " + serverID + ": " + _serverName);
							
							// Configure and send server status.
							final ServerStatus st = new ServerStatus();
							if (ServerConfig.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							
							if (ServerConfig.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							
							if (GeneralConfig.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							
							if (GeneralConfig.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							
							sendPacket(st);
							
							// Send list of currently online players.
							if (World.getAllPlayersCount() > 0)
							{
								final List<String> playerList = new ArrayList<>();
								for (Player player : World.getInstance().getPlayers())
								{
									playerList.add(player.getAccountName());
								}
								
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						}
						case 0x03: // PlayerAuthResponse - Player authentication result.
						{
							final PlayerAuthResponse par = new PlayerAuthResponse(incoming);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							
							// Find the waiting client for this account.
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
										break;
									}
								}
							}
							
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									// Authentication successful - setup client.
									final PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setConnectionState(ConnectionState.AUTHENTICATED);
									wcToRemove.gameClient.setSessionId(wcToRemove.sessionKey);
									final CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									// Authentication failed - close connection.
									LOGGER.warning(getClass().getSimpleName() + ": Session key incorrect. Closing connection for account " + wcToRemove.account);
									wcToRemove.gameClient.sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									_accountsInGameServer.remove(wcToRemove.account);
								}
								
								_waitingClients.remove(wcToRemove);
							}
							break;
						}
						case 0x04: // KickPlayer - Force disconnect player.
						{
							final KickPlayer kp = new KickPlayer(incoming);
							doKickPlayer(kp.getAccount());
							break;
						}
					}
				}
			}
			catch (UnknownHostException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Unknown host: ", e);
			}
			catch (SocketException e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": LoginServer not available, trying to reconnect...");
			}
			catch (IOException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Disconnected from Login, trying to reconnect: ", e);
			}
			finally
			{
				// Clean up connection resources.
				try
				{
					_socket.close();
					if (isInterrupted())
					{
						return;
					}
				}
				catch (Exception e)
				{
					// Ignore cleanup exceptions.
				}
			}
			
			// Wait before attempting reconnection.
			try
			{
				Thread.sleep(RECONNECT_DELAY);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				break;
			}
		}
	}
	
	/**
	 * Adds waiting client and sends authentication request to login server.
	 * @param accountName the account name
	 * @param client the game client
	 * @param key the session key
	 */
	public void addWaitingClientAndSendRequest(String accountName, GameClient client, SessionKey key)
	{
		synchronized (_waitingClients)
		{
			_waitingClients.add(new WaitingClient(accountName, client, key));
		}
		
		sendPacket(new PlayerAuthRequest(accountName, key));
	}
	
	/**
	 * Removes waiting client from authentication queue.
	 * @param client the game client to remove
	 */
	public void removeWaitingClient(GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
					break;
				}
			}
			
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}
	
	/**
	 * Sends logout notification for specified account.
	 * @param account the account name
	 */
	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}
		
		final GameClient removed = _accountsInGameServer.remove(account);
		if (removed != null)
		{
			removed.disconnect();
		}
		
		sendPacket(new PlayerLogout(account));
	}
	
	/**
	 * Adds game server login entry for account tracking.
	 * @param account the account name
	 * @param client the game client
	 * @return true if account login was successful, false otherwise
	 */
	public boolean addGameServerLogin(String account, GameClient client)
	{
		final GameClient savedClient = _accountsInGameServer.get(account);
		if (savedClient != null)
		{
			if (savedClient.isDetached())
			{
				_accountsInGameServer.put(account, client);
				return true;
			}
			
			savedClient.disconnect();
			_accountsInGameServer.remove(account);
			return false;
		}
		
		_accountsInGameServer.put(account, client);
		return true;
	}
	
	/**
	 * Sends access level change notification to login server.
	 * @param account the account name
	 * @param level the new access level
	 */
	public void sendAccessLevel(String account, int level)
	{
		sendPacket(new ChangeAccessLevel(account, level));
	}
	
	/**
	 * Converts hex byte array to hexadecimal string representation.
	 * @param hex the hex byte array
	 * @return the hex string
	 */
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	/**
	 * Forcibly kicks player from the game server.
	 * @param account the account name to kick
	 */
	public void doKickPlayer(String account)
	{
		final GameClient client = _accountsInGameServer.get(account);
		if (client != null)
		{
			client.close(ServerClose.STATIC_PACKET);
			getInstance().sendLogout(account);
		}
		else // Offline player restored after restart.
		{
			boolean logoutSent = false;
			for (Player player : World.getInstance().getPlayers())
			{
				if (account.equals(player.getAccountName()))
				{
					final GameClient playerGameClient = player.getClient();
					if (playerGameClient != null)
					{
						playerGameClient.close(ServerClose.STATIC_PACKET);
					}
					player.storeMe();
					player.deleteMe();
					
					if (!logoutSent)
					{
						logoutSent = true;
						getInstance().sendLogout(account);
					}
				}
			}
		}
	}
	
	/**
	 * Sends packet to login server with proper encryption and checksums.
	 * @param packet the packet to send
	 */
	private void sendPacket(BaseWritablePacket packet)
	{
		if ((_blowfish == null) || (_socket == null) || _socket.isClosed())
		{
			return;
		}
		
		try
		{
			packet.write(); // Write initial packet data.
			packet.writeInt(0); // Reserved space for checksum.
			int size = packet.getLength() - 2; // Size without header.
			
			// Add padding to align packet to 8-byte boundary.
			final int padding = size % PACKET_PADDING;
			if (padding != 0)
			{
				for (int i = padding; i < PACKET_PADDING; i++)
				{
					packet.writeByte(0);
				}
			}
			
			// Get final packet data for encryption.
			final byte[] data = packet.getSendableBytes();
			size = data.length - 2; // Data size without header.
			
			synchronized (_out)
			{
				NewCrypt.appendChecksum(data, 2, size);
				_blowfish.crypt(data, 2, size);
				
				_out.write(data);
				try
				{
					_out.flush();
				}
				catch (IOException e)
				{
					// LoginServer might have terminated connection.
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("LoginServerThread: IOException while sending packet " + packet.getClass().getSimpleName());
			LOGGER.severe(TraceUtil.getStackTrace(e));
		}
	}
	
	/**
	 * Sets maximum player count and notifies login server.
	 * @param maxPlayer the maximum player count
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	/**
	 * Gets current maximum player count.
	 * @return the maximum player count
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	/**
	 * Sends server status update to login server.
	 * @param id the status attribute identifier
	 * @param value the status value
	 */
	public void sendServerStatus(int id, int value)
	{
		final ServerStatus serverStatus = new ServerStatus();
		serverStatus.addAttribute(id, value);
		sendPacket(serverStatus);
	}
	
	/**
	 * Gets server status as human-readable string.
	 * @return the status string representation
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	/**
	 * Checks if server clock is shown in server list.
	 * @return true if clock is shown, false otherwise
	 */
	public boolean isClockShown()
	{
		return ServerConfig.SERVER_LIST_CLOCK;
	}
	
	/**
	 * Gets current server status.
	 * @return the server status code
	 */
	public int getServerStatus()
	{
		return _status;
	}
	
	/**
	 * Checks if server brackets are shown in server list.
	 * @return true if brackets are shown, false otherwise
	 */
	public boolean isBracketShown()
	{
		return ServerConfig.SERVER_LIST_BRACKET;
	}
	
	/**
	 * Gets registered server name.
	 * @return the server name
	 */
	public String getServerName()
	{
		return _serverName;
	}
	
	/**
	 * Sets server status and notifies login server.
	 * @param status the new server status
	 */
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
			case ServerStatus.STATUS_DOWN:
			case ServerStatus.STATUS_FULL:
			case ServerStatus.STATUS_GM_ONLY:
			case ServerStatus.STATUS_GOOD:
			case ServerStatus.STATUS_NORMAL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, status);
				_status = status;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid server status: " + status);
			}
		}
	}
	
	/**
	 * Gets game client for specified account name.
	 * @param name the account name
	 * @return the game client or null if not found
	 */
	public GameClient getClient(String name)
	{
		return name != null ? _accountsInGameServer.get(name) : null;
	}
	
	/**
	 * Session key container for player authentication between login and game servers.
	 */
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	/**
	 * Represents a client waiting for authentication response from login server.
	 */
	private static class WaitingClient
	{
		public String account;
		public GameClient gameClient;
		public SessionKey sessionKey;
		
		public WaitingClient(String acc, GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			sessionKey = key;
		}
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread INSTANCE = new LoginServerThread();
	}
}
