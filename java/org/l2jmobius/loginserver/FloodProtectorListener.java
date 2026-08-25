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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.loginserver.config.LoginConfig;

/**
 * @author -Wooden-
 */
public abstract class FloodProtectorListener extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(FloodProtectorListener.class.getName());
	
	private final Map<String, ForeignConnection> _floodProtection = new ConcurrentHashMap<>();
	private final String _listenIp;
	private final int _port;
	private ServerSocket _serverSocket;
	
	public FloodProtectorListener(String listenIp, int port) throws IOException
	{
		_port = port;
		_listenIp = listenIp;
		
		if (_listenIp.equals("0.0.0.0"))
		{
			_serverSocket = new ServerSocket(_port);
		}
		else
		{
			_serverSocket = new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
		}
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			Socket connection = null;

			try
			{
				connection = _serverSocket.accept(); // Accept incoming connections.
				if (LoginConfig.FLOOD_PROTECTION)
				{
					// Check for flood protection on the connection.
					ForeignConnection fConnection = _floodProtection.get(connection.getInetAddress().getHostAddress());
					if (fConnection != null) // If there's an existing connection from this IP.
					{
						// Read once, so the three tests below all see the same count.
						final int connections = fConnection.connectionNumber.incrementAndGet();
						if (((connections > LoginConfig.FAST_CONNECTION_LIMIT) && ((System.currentTimeMillis() - fConnection.lastConnection) < LoginConfig.NORMAL_CONNECTION_TIME)) || ((System.currentTimeMillis() - fConnection.lastConnection) < LoginConfig.FAST_CONNECTION_TIME) || (connections > LoginConfig.MAX_CONNECTION_PER_IP))
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							fConnection.connectionNumber.decrementAndGet();
							if (!fConnection.isFlooding)
							{
								LOGGER.warning("Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							
							fConnection.isFlooding = true;
							continue;
						}
						
						if (fConnection.isFlooding) // if connection was flooding server but now passed the check
						{
							fConnection.isFlooding = false;
							LOGGER.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
						}
						
						fConnection.lastConnection = System.currentTimeMillis();
					}
					else // If it's a new connection.
					{
						// Initialize flood protection for the new connection.
						fConnection = new ForeignConnection(System.currentTimeMillis());
						_floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
					}
				}
				
				// Add client connection for further processing (implementation in subclasses).
				addClient(connection);
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "FloodProtectorListener: Error processing connection.", e);
				try
				{
					if (connection != null)
					{
						connection.close();
					}
				}
				catch (Exception e2)
				{
					LOGGER.log(Level.FINE, "FloodProtectorListener: Error closing connection.", e2);
				}
				
				if (isInterrupted())
				{
					// Close server socket and break the loop on thread interruption.
					try
					{
						_serverSocket.close();
					}
					catch (IOException io)
					{
						LOGGER.log(Level.INFO, "", io);
					}
					break;
				}
			}
		}
	}
	
	protected static class ForeignConnection
	{
		public ForeignConnection(long time)
		{
			// Initialize a new foreign connection.
			lastConnection = time;
			connectionNumber = new AtomicInteger(1);
		}
		
		// Incremented on the accept thread and decremented from each client's own thread
		// as it disconnects, so a plain int loses updates: the count drifts up and honest
		// connections start being refused as flooding, or drifts down and the entry never
		// reaches zero to be dropped.
		public final AtomicInteger connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;
	}
	
	public abstract void addClient(Socket s);
	
	public void removeFloodProtection(String ip)
	{
		// Only proceed if flood protection is enabled.
		if (!LoginConfig.FLOOD_PROTECTION)
		{
			return;
		}
		
		// Decrement connection count or remove if no more connections exist.
		final ForeignConnection fConnection = _floodProtection.get(ip);
		if (fConnection != null)
		{
			// At or below zero rather than exactly zero: an exact test leaves the entry
			// behind for good if the count ever passes it.
			if (fConnection.connectionNumber.decrementAndGet() <= 0)
			{
				_floodProtection.remove(ip);
			}
		}
		else
		{
			LOGGER.warning("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
		}
	}
	
	public void close()
	{
		try
		{
			_serverSocket.close();
		}
		catch (IOException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
