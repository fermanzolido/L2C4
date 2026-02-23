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
package org.l2jmobius.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.commons.util.TraceUtil;
import org.l2jmobius.loginserver.config.LoginConfig;
import org.l2jmobius.loginserver.network.AbstractServerPacket;
import org.l2jmobius.loginserver.network.gameserverpackets.BlowFishKey;
import org.l2jmobius.loginserver.network.gameserverpackets.ChangeAccessLevel;
import org.l2jmobius.loginserver.network.gameserverpackets.GameServerAuth;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerAuthRequest;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerInGame;
import org.l2jmobius.loginserver.network.gameserverpackets.PlayerLogout;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.loginserver.network.loginserverpackets.AuthResponse;
import org.l2jmobius.loginserver.network.loginserverpackets.InitLS;
import org.l2jmobius.loginserver.network.loginserverpackets.KickPlayer;
import org.l2jmobius.loginserver.network.loginserverpackets.LoginServerFail;
import org.l2jmobius.loginserver.network.loginserverpackets.PlayerAuthResponse;

/**
 * Handles connection and communication between login server and game server.<br>
 * Manages authentication, packet exchange and player account tracking for a single game server connection.
 * <ul>
 * <li>RSA key exchange for secure communication</li>
 * <li>Blowfish encryption setup for packet encryption</li>
 * <li>Game server authentication and registration</li>
 * <li>Continuous packet processing and forwarding</li>
 * <li>Player account management and tracking</li>
 * </ul>
 * @author -Wooden-, Mobius
 */
public class GameServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(GameServerThread.class.getName());
	
	// Constants.
	private static final int PACKET_HEADER_SIZE = 2;
	private static final int HIGH_BYTE_MULTIPLIER = 256;
	private static final String DEFAULT_BLOWFISH_KEY = "_;v.]05-31!|+-%xT!^[$\00";
	private static final String WILDCARD_HOST = "*";
	private static final String LOCALHOST_IP = "127.0.0.1";
	
	// Network Communication.
	private final Socket _socket;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	
	// Cryptography.
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfishCipher;
	private byte[] _blowfishKey;
	
	// Connection State Management.
	private boolean _isAuthed = false;
	private final String _connectionIp;
	private String _connectionIpAddress;
	
	// Player Account Management.
	private final Set<String> _accountsInGame = ConcurrentHashMap.newKeySet();
	
	// Game Server Configuration.
	private int _serverId;
	private int _maxPlayers;
	private boolean _isTestServer;
	private boolean _pvpServer;
	private int _gamePort;
	private byte[] _hexID;
	private String _gameExternalHost;
	private String _gameInternalHost;
	private String _gameExternalIP;
	private String _gameInternalIP;
	
	/**
	 * Constructs a new GameServerThread for handling communication with a game server.<br>
	 * Initializes the network streams, RSA key pair and Blowfish encryption. The thread automatically starts upon construction.
	 * @param socket the TCP socket connection to the game server
	 */
	public GameServerThread(Socket socket)
	{
		_socket = socket;
		_connectionIp = socket.getInetAddress().getHostAddress();
		
		try
		{
			// Initialize network streams for communication.
			_inputStream = _socket.getInputStream();
			_outputStream = new BufferedOutputStream(_socket.getOutputStream());
		}
		catch (IOException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Failed to initialize network streams - " + e.getMessage());
		}
		
		// Retrieve RSA key pair for secure initial communication.
		final KeyPair keyPair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) keyPair.getPrivate();
		_publicKey = (RSAPublicKey) keyPair.getPublic();
		
		// Initialize Blowfish cipher with default key for packet encryption.
		_blowfishCipher = new NewCrypt(DEFAULT_BLOWFISH_KEY);
		
		// Start the thread to begin processing.
		start();
	}
	
	/**
	 * Main thread execution method that handles the game server connection lifecycle.<br>
	 * Processes incoming packets continuously until connection is terminated.
	 */
	@Override
	public void run()
	{
		try
		{
			// Send initial packet with RSA public key for secure key exchange.
			final InitLS initializationPacket = new InitLS(_publicKey.getModulus().toByteArray());
			sendPacket(initializationPacket);
			
			// Resolve and store the connection IP address.
			_connectionIpAddress = _socket.getInetAddress().getHostAddress();
			
			// Check if the connecting IP is banned.
			if (isBannedGameserverIP(_connectionIpAddress))
			{
				final LoginServerFail bannedPacket = new LoginServerFail(LoginServerFail.REASON_IP_BANNED);
				sendPacket(bannedPacket);
				LOGGER.info("GameServerRegistration: IP Address " + _connectionIpAddress + " is on the banned IP list. Connection rejected.");
				return; // Terminate processing for banned IP.
			}
			
			// Main packet processing loop.
			int lengthHighByte = 0;
			int lengthLowByte = 0;
			int packetLength = 0;
			boolean checksumValid = false;
			
			while (true)
			{
				// Read packet length header (2 bytes: low byte, high byte).
				lengthLowByte = _inputStream.read();
				lengthHighByte = _inputStream.read();
				packetLength = (lengthHighByte * HIGH_BYTE_MULTIPLIER) + lengthLowByte;
				
				// Check for connection termination.
				if ((lengthHighByte < 0) || _socket.isClosed())
				{
					LOGGER.finer("GameServerThread: Game server terminated the connection gracefully.");
					break;
				}
				
				// Allocate buffer for packet data (excluding header).
				byte[] packetData = new byte[packetLength - PACKET_HEADER_SIZE];
				
				int totalBytesReceived = 0;
				int bytesRead = 0;
				int remainingBytes = packetLength - PACKET_HEADER_SIZE;
				
				// Read the complete packet data.
				while ((bytesRead != -1) && (totalBytesReceived < (packetLength - PACKET_HEADER_SIZE)))
				{
					bytesRead = _inputStream.read(packetData, totalBytesReceived, remainingBytes);
					totalBytesReceived = totalBytesReceived + bytesRead;
					remainingBytes -= bytesRead;
				}
				
				// Validate that we received the complete packet.
				if (totalBytesReceived != (packetLength - PACKET_HEADER_SIZE))
				{
					LOGGER.warning("Incomplete packet received from game server. Expected " + (packetLength - PACKET_HEADER_SIZE) + " bytes, got " + totalBytesReceived + ". Closing connection.");
					break;
				}
				
				// Decrypt the packet data using Blowfish cipher.
				_blowfishCipher.decrypt(packetData, 0, packetData.length);
				
				// Verify packet integrity using checksum.
				checksumValid = NewCrypt.verifyChecksum(packetData);
				if (!checksumValid)
				{
					LOGGER.warning("Packet checksum verification failed. Possible data corruption or tampering detected. Closing connection.");
					return;
				}
				
				// Process packet based on type.
				final int packetType = packetData[0] & 0xff;
				switch (packetType)
				{
					case 0x00:
					{
						// Handle Blowfish key exchange packet.
						final BlowFishKey blowfishKeyPacket = new BlowFishKey(packetData, _privateKey);
						_blowfishKey = blowfishKeyPacket.getKey();
						_blowfishCipher = new NewCrypt(_blowfishKey);
						break;
					}
					case 0x01:
					{
						// Handle game server authentication request.
						final GameServerAuth authPacket = new GameServerAuth(packetData);
						handleRegistrationProcess(authPacket);
						if (_isAuthed)
						{
							final AuthResponse authResponse = new AuthResponse(_serverId);
							sendPacket(authResponse);
						}
						else
						{
							LOGGER.info("Game server authentication failed. Closing connection.");
							_socket.close();
						}
						break;
					}
					case 0x02:
					{
						if (!_isAuthed)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(failPacket);
							_socket.close();
							break;
						}
						
						// Handle player login notification.
						final PlayerInGame playerInGamePacket = new PlayerInGame(packetData);
						for (String accountName : playerInGamePacket.getAccounts())
						{
							_accountsInGame.add(accountName);
						}
						break;
					}
					case 0x03:
					{
						if (!_isAuthed)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(failPacket);
							_socket.close();
							break;
						}
						
						// Handle player logout notification.
						final PlayerLogout logoutPacket = new PlayerLogout(packetData);
						_accountsInGame.remove(logoutPacket.getAccount());
						break;
					}
					case 0x04:
					{
						if (!_isAuthed)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(failPacket);
							_socket.close();
							break;
						}
						
						// Handle access level change request.
						final ChangeAccessLevel accessLevelPacket = new ChangeAccessLevel(packetData);
						LoginController.getInstance().setAccountAccessLevel(accessLevelPacket.getAccount(), accessLevelPacket.getLevel());
						LOGGER.info("Changed " + accessLevelPacket.getAccount() + " access level to " + accessLevelPacket.getLevel());
						break;
					}
					case 0x05:
					{
						if (!_isAuthed)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(failPacket);
							_socket.close();
							break;
						}
						
						// Handle player authentication verification.
						final PlayerAuthRequest authRequest = new PlayerAuthRequest(packetData);
						final SessionKey sessionKey = LoginController.getInstance().getKeyForAccount(authRequest.getAccount());
						if ((sessionKey != null) && sessionKey.equals(authRequest.getKey()))
						{
							sendPacket(new PlayerAuthResponse(authRequest.getAccount(), true));
						}
						else
						{
							sendPacket(new PlayerAuthResponse(authRequest.getAccount(), false));
						}
						
						LoginController.getInstance().removeLoginClient(authRequest.getAccount());
						break;
					}
					case 0x06:
					{
						if (!_isAuthed)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.NOT_AUTHED);
							sendPacket(failPacket);
							_socket.close();
							break;
						}
						
						// Handle server status update.
						@SuppressWarnings("unused")
						final ServerStatus serverStatusPacket = new ServerStatus(packetData, _serverId); // Performs actions automatically.
						break;
					}
				}
			}
		}
		catch (IOException e)
		{
			// Log connection loss with server identification.
			final String serverIdentification = (_serverId != -1 ? "[" + _serverId + "] " + GameServerTable.getInstance()._serverNames.get(_serverId) : "(" + _connectionIpAddress + ")");
			final String connectionLostMessage = "GameServer " + serverIdentification + ": Connection lost - " + e.getMessage();
			LOGGER.info(connectionLostMessage);
		}
		finally
		{
			// Cleanup operations when thread terminates.
			if (_isAuthed)
			{
				GameServerTable.getInstance().setServerReallyDown(_serverId);
				LOGGER.info("Server " + GameServerTable.getInstance()._serverNames.get(_serverId) + " (" + _serverId + ") is now marked as disconnected.");
			}
			
			// Remove this game server from active connections and cleanup flood protection.
			LoginServer.getGameServerListener().removeGameServer(this);
			LoginServer.getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	/**
	 * Handles the game server registration and authentication process.<br>
	 * Validates server credentials and configures server settings based on registration type.
	 * @param gameServerAuth the authentication packet containing server details
	 */
	private void handleRegistrationProcess(GameServerAuth gameServerAuth)
	{
		try
		{
			if (GameServerTable.getInstance().isARegisteredServer(gameServerAuth.getHexID()))
			{
				// Handle registered server authentication.
				_serverId = GameServerTable.getInstance().getServerIDforHex(gameServerAuth.getHexID());
				if (GameServerTable.getInstance().isServerAuthed(_serverId))
				{
					final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.REASON_ALREADY_LOGGED8IN);
					sendPacket(failPacket);
					_socket.close();
					return;
				}
				
				// Configure server settings for registered server.
				_gamePort = gameServerAuth.getPort();
				setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
				_maxPlayers = gameServerAuth.getMaxPlayers();
				_hexID = gameServerAuth.getHexID();
				GameServerTable.getInstance().addServer(this);
			}
			else if (LoginConfig.ACCEPT_NEW_GAMESERVER)
			{
				// Handle new server registration.
				if (!gameServerAuth.acceptAlternateID())
				{
					// Server requests specific ID without alternatives.
					if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(gameServerAuth.getDesiredID()))
					{
						_serverId = gameServerAuth.getDesiredID();
						_gamePort = gameServerAuth.getPort();
						setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
						_maxPlayers = gameServerAuth.getMaxPlayers();
						_hexID = gameServerAuth.getHexID();
						GameServerTable.getInstance().createServer(this);
						GameServerTable.getInstance().addServer(this);
					}
					else
					{
						final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.REASON_ID_RESERVED);
						sendPacket(failPacket);
						_socket.close();
						return;
					}
				}
				else
				{
					// Server accepts alternative ID if desired ID is taken.
					int availableId;
					if (GameServerTable.getInstance().hasRegisteredGameServerOnId(gameServerAuth.getDesiredID()))
					{
						availableId = GameServerTable.getInstance().findFreeID();
						if (availableId < 0)
						{
							final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.REASON_NO_FREE_ID);
							sendPacket(failPacket);
							_socket.close();
							return;
						}
					}
					else
					{
						availableId = gameServerAuth.getDesiredID();
					}
					
					// Configure server settings for new server.
					_serverId = availableId;
					_gamePort = gameServerAuth.getPort();
					setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
					_maxPlayers = gameServerAuth.getMaxPlayers();
					_hexID = gameServerAuth.getHexID();
					GameServerTable.getInstance().createServer(this);
					GameServerTable.getInstance().addServer(this);
				}
			}
			else
			{
				// New servers not accepted, reject connection.
				LOGGER.info("Unregistered game server with invalid HexID attempted to connect. Connection rejected.");
				final LoginServerFail failPacket = new LoginServerFail(LoginServerFail.REASON_WRONG_HEXID);
				sendPacket(failPacket);
				_socket.close();
				return;
			}
			
			// Mark server as authenticated.
			_isAuthed = true;
		}
		catch (IOException e)
		{
			LOGGER.warning("Error occurred while registering GameServer " + GameServerTable.getInstance()._serverNames.get(_serverId) + " (ID: " + _serverId + ") - " + e.getMessage());
		}
	}
	
	/**
	 * Checks if the specified IP address is banned from connecting as a game server
	 * @param ipAddress the IP address to check
	 * @return true if the IP address is banned, false otherwise
	 */
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		// IP banning logic not implemented.
		return false;
	}
	
	/**
	 * Sends a packet to the connected game server with proper encryption and checksums.<br>
	 * Packet is automatically encrypted using Blowfish cipher with integrity verification.
	 * @param packet the packet to send to the game server
	 */
	private void sendPacket(AbstractServerPacket packet)
	{
		try
		{
			// Get packet data and prepare for transmission.
			final byte[] packetData = packet.getContent();
			
			// Thread-safe packet transmission.
			synchronized (_outputStream)
			{
				// Append checksum for integrity verification.
				NewCrypt.appendChecksum(packetData);
				
				// Encrypt the packet data.
				_blowfishCipher.crypt(packetData, 0, packetData.length);
				
				// Send packet with length header.
				final int totalLength = packetData.length + PACKET_HEADER_SIZE;
				_outputStream.write(totalLength & 0xff);
				_outputStream.write((totalLength >> 8) & 0xff);
				_outputStream.write(packetData);
				
				try
				{
					_outputStream.flush();
				}
				catch (IOException e)
				{
					// Game server might have terminated connection during flush.
					LOGGER.finer("GameServerThread: Failed to flush output stream. Game server may have disconnected - " + e.getMessage());
				}
			}
		}
		catch (IOException e)
		{
			LOGGER.severe("GameServerThread: IOException occurred while sending packet " + packet.getClass().getSimpleName() + " - " + e.getMessage());
			LOGGER.severe(TraceUtil.getStackTrace(e));
		}
	}
	
	/**
	 * Sends a kick command to the game server to disconnect a specific player.
	 * @param accountName the account name of the player to kick
	 */
	public void kickPlayer(String accountName)
	{
		sendPacket(new KickPlayer(accountName));
	}
	
	/**
	 * Configures the game server's host addresses.<br>
	 * Updates server network configuration and resolves IP addresses for external and internal hosts.
	 * @param gameExternalHost the external host address
	 * @param gameInternalHost the internal host address
	 */
	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		final String oldInternalHost = _gameInternalHost;
		final String oldExternalHost = _gameExternalHost;
		_gameExternalHost = gameExternalHost;
		_gameInternalHost = gameInternalHost;
		
		// Resolve external host IP address.
		if (!_gameExternalHost.equals(WILDCARD_HOST))
		{
			try
			{
				_gameExternalIP = InetAddress.getByName(_gameExternalHost).getHostAddress();
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Failed to resolve external hostname \"" + _gameExternalHost + "\" - " + e.getMessage());
			}
		}
		else
		{
			_gameExternalIP = _connectionIp;
		}
		
		// Resolve internal host IP address.
		if (!_gameInternalHost.equals(WILDCARD_HOST))
		{
			try
			{
				_gameInternalIP = InetAddress.getByName(_gameInternalHost).getHostAddress();
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Failed to resolve internal hostname \"" + _gameInternalHost + "\" - " + e.getMessage());
			}
		}
		else
		{
			_gameInternalIP = _connectionIp;
		}
		
		// Log IP address updates.
		final String serverName = GameServerTable.getInstance()._serverNames.get(_serverId);
		if ((oldInternalHost == null) || !oldInternalHost.equalsIgnoreCase(_gameInternalIP))
		{
			LOGGER.info("Updated Gameserver " + serverName + " Internal IP to: " + getGameInternalIP());
		}
		
		if ((oldExternalHost == null) || !oldExternalHost.equalsIgnoreCase(_gameExternalIP))
		{
			LOGGER.info("Updated Gameserver " + serverName + " External IP to: " + getGameExternalIP());
		}
	}
	
	/**
	 * Returns the unique hex ID of this game server.
	 * @return the hex ID as byte array
	 */
	public byte[] getHexID()
	{
		return _hexID;
	}
	
	/**
	 * Returns the set of player accounts currently logged into this game server.
	 * @return set of account names in game
	 */
	public Set<String> getPlayersInGame()
	{
		return _accountsInGame;
	}
	
	/**
	 * Returns the maximum number of players this server can accommodate.
	 * @return the maximum player count
	 */
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	/**
	 * Returns the current number of players logged into this game server.
	 * @return the current player count
	 */
	public int getCurrentPlayers()
	{
		return _accountsInGame.size();
	}
	
	/**
	 * Returns the unique server ID of this game server.
	 * @return the server ID
	 */
	public int getServerID()
	{
		return _serverId;
	}
	
	/**
	 * Returns the external game host address.
	 * @return the external host address
	 */
	public String getGameExternalHost()
	{
		return _gameExternalHost;
	}
	
	/**
	 * Returns the internal game host address.
	 * @return the internal host address
	 */
	public String getGameInternalHost()
	{
		return _gameInternalHost;
	}
	
	/**
	 * Returns the game server port number.
	 * @return the port number
	 */
	public int getPort()
	{
		return _gamePort;
	}
	
	/**
	 * Returns whether this is a PvP server.
	 * @return true if PvP server, false otherwise
	 */
	public boolean getPvP()
	{
		return _pvpServer;
	}
	
	/**
	 * Returns whether this is a test server.
	 * @return true if test server, false otherwise
	 */
	public boolean isTestServer()
	{
		return _isTestServer;
	}
	
	/**
	 * Returns the game server's external IP address.
	 * @return the external IP address, or localhost if not set
	 */
	public String getGameExternalIP()
	{
		return _gameExternalIP == null ? LOCALHOST_IP : _gameExternalIP;
	}
	
	/**
	 * Returns the game server's internal IP address.
	 * @return the internal IP address, or localhost if not set
	 */
	public String getGameInternalIP()
	{
		return _gameInternalIP == null ? LOCALHOST_IP : _gameInternalIP;
	}
	
	/**
	 * Returns whether this game server is authenticated.
	 * @return true if authenticated, false otherwise
	 */
	public boolean isAuthed()
	{
		return _isAuthed;
	}
	
	/**
	 * Sets the authentication status of this game server.
	 * @param isAuthed the authentication status to set
	 */
	public void setAuthed(boolean isAuthed)
	{
		_isAuthed = isAuthed;
	}
	
	/**
	 * Sets the maximum number of players this server can accommodate.
	 * @param value the maximum player count
	 */
	public void setMaxPlayers(int value)
	{
		_maxPlayers = value;
	}
	
	/**
	 * Returns the IP address of the connected game server.
	 * @return the connection IP address
	 */
	public String getConnectionIpAddress()
	{
		return _connectionIpAddress;
	}
}
