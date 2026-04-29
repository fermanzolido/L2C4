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
package org.l2jmobius.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author Kerberos, JIV
 * @version 8/24/10
 */
/**
 * Manager for tracking and calculating Raid Boss points and rankings.
 * @author Kerberos, JIV
 * @version 8/24/10
 */
public class RaidBossPointsManager
{
	private static final Logger LOGGER = Logger.getLogger(RaidBossPointsManager.class.getName());
	
	private final Map<Integer, Map<Integer, Integer>> _list = new ConcurrentHashMap<>();
	// Cache for total points per player to optimize ranking calculations and point retrieval.
	// This reduces getPointsByOwnerId from O(M) to O(1) and calculateRanking from O(N log N) to O(N).
	private final Map<Integer, Integer> _totalPoints = new ConcurrentHashMap<>();
	
	public RaidBossPointsManager()
	{
		init();
	}
	
	private void init()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT `charId`,`boss_id`,`points` FROM `character_raid_points`"))
		{
			while (rs.next())
			{
				final int charId = rs.getInt("charId");
				final int bossId = rs.getInt("boss_id");
				final int points = rs.getInt("points");
				Map<Integer, Integer> values = _list.get(charId);
				if (values == null)
				{
					values = new HashMap<>();
				}
				
				values.put(bossId, points);
				_list.put(charId, values);
				_totalPoints.merge(charId, points, Integer::sum);
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _list.size() + " Characters Raid Points.");
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not load raid points ", e);
		}
	}
	
	public void updatePointsInDB(Player player, int raidId, int points)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)"))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, raidId);
			ps.setInt(3, points);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update char raid points for player: " + player, e);
		}
	}
	
	public void addPoints(Player player, int bossId, int points)
	{
		final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(player.getObjectId(), unused -> new HashMap<>());
		updatePointsInDB(player, bossId, tmpPoint.merge(bossId, points, Integer::sum));
		_totalPoints.merge(player.getObjectId(), points, Integer::sum);
	}
	
	/**
	 * Gets the total raid points for a given player.
	 * Optimized to O(1) using the cached total points map.
	 * @param ownerId the object ID of the player
	 * @return the total raid points
	 */
	public int getPointsByOwnerId(int ownerId)
	{
		return _totalPoints.getOrDefault(ownerId, 0);
	}
	
	public Map<Integer, Integer> getList(Player player)
	{
		return _list.get(player.getObjectId());
	}
	
	public void cleanUp()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0"))
		{
			statement.executeUpdate();
			_list.clear();
			_totalPoints.clear();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't clean raid points", e);
		}
	}
	
	/**
	 * Calculates the ranking of a player based on their total raid points.
	 * Optimized to O(N) by counting players with more points than the target player.
	 * This avoids the expensive O(N log N) full sort previously used.
	 * @param playerObjId the object ID of the player
	 * @return the player's ranking, or 0 if they have no points
	 */
	public int calculateRanking(int playerObjId)
	{
		final int points = getPointsByOwnerId(playerObjId);
		if (points == 0)
		{
			return 0;
		}
		
		int ranking = 1;
		for (int totalPoints : _totalPoints.values())
		{
			if (totalPoints > points)
			{
				ranking++;
			}
		}
		return ranking;
	}

	/**
	 * Gets the full ranking list.
	 * Note: This method is expensive (O(N log N)) and should be avoided if only individual rankings are needed.
	 * @return a map of player object IDs to their ranking
	 */
	public Map<Integer, Integer> getRankList()
	{
		final List<Entry<Integer, Integer>> list = new ArrayList<>(_totalPoints.entrySet());
		list.sort(Comparator.comparing(Entry<Integer, Integer>::getValue).reversed());

		int ranking = 1;
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		for (Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		
		return tmpRanking;
	}
	

	public static RaidBossPointsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossPointsManager INSTANCE = new RaidBossPointsManager();
	}
}
