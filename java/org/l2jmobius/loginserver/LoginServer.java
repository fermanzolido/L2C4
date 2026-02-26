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
package org.l2jmobius.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.l2jmobius.commons.config.DatabaseConfig;
import org.l2jmobius.commons.config.InterfaceConfig;
import org.l2jmobius.commons.database.DatabaseBackup;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.loginserver.config.LoginConfig;
import org.l2jmobius.loginserver.network.AbstractClientPacket;
import org.l2jmobius.loginserver.network.LoginClient;
import org.l2jmobius.loginserver.ui.Gui;

/**
 * This class ...
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends FloodProtectorListener
{
	public static final Logger LOGGER = Logger.getLogger(LoginServer.class.getName());
	
	public static final int PROTOCOL_REV = 0x0102;
	
	private static LoginServer _instance;
	private static GameServerListener _gameServerListener;
	private ServerSocket _serverSocket;
	
	private LoginServer() throws Exception
	{
		super(LoginConfig.LOGIN_BIND_ADDRESS, LoginConfig.PORT_LOGIN);
		
		// GUI.
		InterfaceConfig.load();
		if (InterfaceConfig.ENABLE_GUI)
		{
			LOGGER.info("Running in GUI mode.");
			new Gui();
		}
		
		// Prepare the database.
		DatabaseFactory.init();
		
		// Initialize ThreadPool.
		ThreadPool.init();
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		GameServerTable.getInstance();
		
		loadBanFile();
		
		if (LoginConfig.LOGIN_SERVER_SCHEDULE_RESTART)
		{
			LOGGER.info("Scheduled LS restart after " + LoginConfig.LOGIN_SERVER_SCHEDULE_RESTART_TIME + " hours.");
			ThreadPool.schedule(() -> shutdown(true), LoginConfig.LOGIN_SERVER_SCHEDULE_RESTART_TIME * 3600000);
		}
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			LOGGER.info("Listening for GameServers on " + LoginConfig.GAME_SERVER_LOGIN_HOST + ":" + LoginConfig.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
	}
	
	public static GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	public void loadBanFile()
	{
		final File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			try (FileInputStream fis = new FileInputStream(bannedFile);
				InputStreamReader is = new InputStreamReader(fis);
				LineNumberReader lnr = new LineNumberReader(is))
			{
				lnr.lines().map(String::trim).filter(l -> !l.isEmpty() && (l.charAt(0) != '#')).forEach(lineValue ->
				{
					String line = lineValue;
					String[] parts = line.split("#", 2); // address[ duration][ # comments]
					line = parts[0];
					parts = line.split("\\s+"); // Durations might be aligned via multiple spaces.
					final String address = parts[0];
					long duration = 0;
					if (parts.length > 1)
					{
						try
						{
							duration = Long.parseLong(parts[1]);
						}
						catch (NumberFormatException nfe)
						{
							LOGGER.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + lnr.getLineNumber());
							return;
						}
					}
					
					try
					{
						LoginController.getInstance().addBanForAddress(address, duration);
					}
					catch (Exception e)
					{
						LOGGER.warning("Skipped: Invalid address (" + address + ") on (" + bannedFile.getName() + "). Line: " + lnr.getLineNumber());
					}
				});
			}
			catch (IOException e)
			{
				LOGGER.log(Level.WARNING, "Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage(), e);
			}
			
			LOGGER.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			LOGGER.warning("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
	}
	
	public boolean unblockIp(String ipAddress)
	{
		if (LoginController.getInstance().ipBlocked(ipAddress))
		{
			return true;
		}
		
		return false;
	}
	
	public static class ForeignConnection
	{
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
		
		public int connectionNumber;
		public long lastConnection;
	}
	
	public void execute(AbstractClientPacket packet)
	{
		try
		{
			ThreadPool.execute(packet);
		}
		catch (RejectedExecutionException e)
		{
			LOGGER.severe("Failed executing: " + packet.getClass().getSimpleName() + " for IP: " + packet.getClient().getSocket().getInetAddress().getHostAddress());
		}
	}
	
	public void shutdown(boolean restart)
	{
		if (DatabaseConfig.BACKUP_DATABASE)
		{
			DatabaseBackup.performBackup("login");
		}
		
		// Shut down executor.
		try
		{
			ThreadPool.shutdown();
		}
		catch (Throwable t)
		{
		}
		
		_gameServerListener.interrupt();
		GameServerTable.getInstance().shutDown();
		
		try
		{
			if (_serverSocket != null)
			{
				_serverSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
	
	@Override
	public void addClient(Socket socket)
	{
		new LoginClient(socket);
	}
	
	public static LoginServer getInstance()
	{
		return _instance;
	}
	
	public static void main(String[] args) throws Exception
	{
		// Create log folder.
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory.
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		catch (IOException e)
		{
			LOGGER.warning("LoginServer: " + e.getMessage());
		}
		
		// Load LoginConfig.
		LoginConfig.load();
		
		try
		{
			_instance = new LoginServer();
			_instance.start();
			LOGGER.info("Login Server ready on " + LoginConfig.LOGIN_BIND_ADDRESS + ":" + LoginConfig.PORT_LOGIN);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Failed to start the Login Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
	}
}
