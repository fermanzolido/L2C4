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
package org.l2jmobius.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.config.custom.PremiumSystemConfig;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenersContainer;
import org.l2jmobius.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author Mobius
 */
public class PremiumManager
{
	private static final Logger LOGGER = Logger.getLogger(PremiumManager.class.getName());
	
	private static final String LOAD_SQL = "SELECT account_name,enddate FROM account_premium WHERE account_name = ?";
	private static final String UPDATE_SQL = "REPLACE INTO account_premium (account_name,enddate) VALUE (?,?)";
	private static final String DELETE_SQL = "DELETE FROM account_premium WHERE account_name = ?";
	private static final String REFRESH_SQL_PREFIX = "SELECT account_name,enddate FROM account_premium WHERE account_name IN (";
	
	class PremiumExpireTask implements Runnable
	{
		final Player _player;
		
		PremiumExpireTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.setPremiumStatus(false);
		}
	}
	
	// Data Cache
	private final Map<String, Long> _premiumData = new ConcurrentHashMap<>();
	
	// expireTasks
	private final Map<String, ScheduledFuture<?>> _expiretasks = new ConcurrentHashMap<>();
	
	// Listeners
	private final ListenersContainer _listenerContainer = Containers.Players();
	
	private final Consumer<OnPlayerLogin> _playerLoginEvent = event ->
	{
		final Player player = event.getPlayer();
		final String accountName = player.getAccountName();
		loadPremiumData(accountName);
		final long now = System.currentTimeMillis();
		final long premiumExpiration = getPremiumExpiration(accountName);
		player.setPremiumStatus(premiumExpiration > now);
		if (player.hasPremiumStatus())
		{
			startExpireTask(player, premiumExpiration - now);
		}
		else if (premiumExpiration > 0)
		{
			removePremiumStatus(accountName, false);
		}
	};
	
	private final Consumer<OnPlayerLogout> _playerLogoutEvent = event ->
	{
		stopExpireTask(event.getPlayer());
	};
	
	protected PremiumManager()
	{
		_listenerContainer.addListener(new ConsumerEventListener(_listenerContainer, EventType.ON_PLAYER_LOGIN, _playerLoginEvent, this));
		_listenerContainer.addListener(new ConsumerEventListener(_listenerContainer, EventType.ON_PLAYER_LOGOUT, _playerLogoutEvent, this));

		if (PremiumSystemConfig.PREMIUM_REFRESH_INTERVAL > 0)
		{
			final long period = PremiumSystemConfig.PREMIUM_REFRESH_INTERVAL * 1000L;
			ThreadPool.scheduleAtFixedRate(this::refreshOnlineAccounts, period, period);
			LOGGER.info("PremiumManager: Re-reading premium of online accounts every " + PremiumSystemConfig.PREMIUM_REFRESH_INTERVAL + " seconds.");
		}
	}

	/**
	 * Re-reads account_premium for every account currently online and applies whatever changed.
	 * <p>
	 * Premium sold outside the game is written straight into the table by whatever provisions it,
	 * and this process never hears about that: the cache is filled once at login and read from
	 * memory afterwards, so a purchase would otherwise sit invisible until the buyer relogged.
	 * <p>
	 * One statement covers every online account rather than one per player, and a query that
	 * fails leaves all of them untouched -- reading an error as "nobody has premium" would strip
	 * the status from everyone who paid for it.
	 */
	private void refreshOnlineAccounts()
	{
		final Map<String, Player> online = new HashMap<>();
		for (Player player : World.getInstance().getPlayers())
		{
			if (player != null)
			{
				online.putIfAbsent(player.getAccountName().toLowerCase(), player);
			}
		}

		if (online.isEmpty())
		{
			return;
		}

		final StringBuilder sql = new StringBuilder(REFRESH_SQL_PREFIX);
		for (int i = 0; i < online.size(); i++)
		{
			sql.append(i == 0 ? "?" : ",?");
		}
		sql.append(')');

		final Map<String, Long> stored = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(sql.toString()))
		{
			int index = 1;
			for (String accountName : online.keySet())
			{
				stmt.setString(index++, accountName);
			}

			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					stored.put(rset.getString(1).toLowerCase(), rset.getLong(2));
				}
			}
		}
		catch (SQLException e)
		{
			// Nothing is applied from a query that did not finish.
			LOGGER.log(Level.WARNING, "Problem with PremiumManager: " + e.getMessage(), e);
			return;
		}

		final long now = System.currentTimeMillis();
		for (Entry<String, Player> entry : online.entrySet())
		{
			final String accountName = entry.getKey();
			final long expiration = stored.getOrDefault(accountName, 0L);
			final Long cached = _premiumData.get(accountName);
			if ((cached != null) && (cached.longValue() == expiration))
			{
				continue;
			}

			_premiumData.put(accountName, expiration);

			final Player player = entry.getValue();
			stopExpireTask(player);
			if (expiration > now)
			{
				player.setPremiumStatus(true);
				startExpireTask(player, expiration - now);
				LOGGER.info("PremiumManager: Account " + accountName + " picked up premium granted outside the game.");
			}
			else
			{
				player.setPremiumStatus(false);
			}
		}
	}
	
	/**
	 * @param player
	 * @param delay
	 */
	private void startExpireTask(Player player, long delay)
	{
		_expiretasks.put(player.getAccountName().toLowerCase(), ThreadPool.schedule(new PremiumExpireTask(player), delay));
	}
	
	/**
	 * @param player
	 */
	private void stopExpireTask(Player player)
	{
		ScheduledFuture<?> task = _expiretasks.remove(player.getAccountName().toLowerCase());
		if (task != null)
		{
			task.cancel(false);
			task = null;
		}
	}
	
	private void loadPremiumData(String accountName)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(LOAD_SQL))
		{
			stmt.setString(1, accountName.toLowerCase());
			long expiration = 0;
			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					expiration = rset.getLong(2);
				}
			}
			
			// The absence of a row is cached too, so that getPremiumExpiration below does not
			// query again on every read for an account that has no premium. Left inside the
			// try, so a failed query is never mistaken for "this account has none".
			_premiumData.put(accountName.toLowerCase(), expiration);
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Problem with PremiumManager: " + e.getMessage(), e);
		}
	}
	
	public long getPremiumExpiration(String accountName)
	{
		// The cache only ever held accounts that logged in since startup, so any account
		// that was offline read as zero. addPremiumTime builds the new expiration on top of
		// this answer and then REPLACEs the row, which silently threw away whatever premium
		// time an offline account still had left.
		Long expiration = _premiumData.get(accountName.toLowerCase());
		if (expiration == null)
		{
			loadPremiumData(accountName);
			expiration = _premiumData.get(accountName.toLowerCase());
		}
		
		return expiration == null ? 0L : expiration.longValue();
	}
	
	public void addPremiumTime(String accountName, int timeValue, TimeUnit timeUnit)
	{
		final long addTime = timeUnit.toMillis(timeValue);
		final long now = System.currentTimeMillis();
		
		// new premium task at least from now
		final long oldPremiumExpiration = Math.max(now, getPremiumExpiration(accountName));
		final long newPremiumExpiration = oldPremiumExpiration + addTime;
		
		// UPDATE DATABASE
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(UPDATE_SQL))
		{
			stmt.setString(1, accountName.toLowerCase());
			stmt.setLong(2, newPremiumExpiration);
			stmt.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Problem with PremiumManager: " + e.getMessage(), e);
		}
		
		// UPDATE CACHE
		_premiumData.put(accountName.toLowerCase(), newPremiumExpiration);
		
		// UPDATE PlAYER PREMIUMSTATUS
		for (Player player : World.getInstance().getPlayers())
		{
			if (accountName.equalsIgnoreCase(player.getAccountName()))
			{
				stopExpireTask(player);
				startExpireTask(player, newPremiumExpiration - now);
				if (!player.hasPremiumStatus())
				{
					player.setPremiumStatus(true);
				}
				break;
			}
		}
	}
	
	public void removePremiumStatus(String accountName, boolean checkOnline)
	{
		if (checkOnline)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (accountName.equalsIgnoreCase(player.getAccountName()) && player.hasPremiumStatus())
				{
					player.setPremiumStatus(false);
					stopExpireTask(player);
					break;
				}
			}
		}
		
		// UPDATE CACHE
		_premiumData.remove(accountName.toLowerCase());
		
		// UPDATE DATABASE
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(DELETE_SQL))
		{
			stmt.setString(1, accountName.toLowerCase());
			stmt.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Problem with PremiumManager: " + e.getMessage(), e);
		}
	}
	
	public static PremiumManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PremiumManager INSTANCE = new PremiumManager();
	}
}
