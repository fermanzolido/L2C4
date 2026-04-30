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
public class RaidBossPointsManager
{
	private static final Logger LOGGER = Logger.getLogger(RaidBossPointsManager.class.getName());
	
	private final Map<Integer, Map<Integer, Integer>> _list = new ConcurrentHashMap<>();
	// Optimization: Cache total points per player to provide O(1) lookups for ranking and UI.
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
				
				_list.computeIfAbsent(charId, k -> new HashMap<>()).put(bossId, points);
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
		final int charId = player.getObjectId();
		final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(charId, unused -> new HashMap<>());
		updatePointsInDB(player, bossId, tmpPoint.merge(bossId, points, Integer::sum));
		_totalPoints.merge(charId, points, Integer::sum);
	}
	
	/**
	 * Optimization: Update multiple players' raid points using JDBC batching to reduce database round-trips.
	 * @param playerPoints the map of players and their newly gained points
	 * @param bossId the boss ID
	 */
	public void addPointsBatch(Map<Player, Integer> playerPoints, int bossId)
	{
		if ((playerPoints == null) || playerPoints.isEmpty())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)"))
		{
			for (Entry<Player, Integer> entry : playerPoints.entrySet())
			{
				final Player player = entry.getKey();
				final int charId = player.getObjectId();
				final int pointsToAdd = entry.getValue();

				final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(charId, unused -> new HashMap<>());
				final int totalBossPoints = tmpPoint.merge(bossId, pointsToAdd, Integer::sum);
				_totalPoints.merge(charId, pointsToAdd, Integer::sum);

				ps.setInt(1, charId);
				ps.setInt(2, bossId);
				ps.setInt(3, totalBossPoints);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update char raid points batch for boss: " + bossId, e);
		}
	}

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
	
	public int calculateRanking(int playerObjId)
	{
		final Map<Integer, Integer> rank = getRankList();
		if (rank.containsKey(playerObjId))
		{
			return rank.get(playerObjId);
		}
		
		return 0;
	}
	
	public Map<Integer, Integer> getRankList()
	{
		final List<Entry<Integer, Integer>> list = new ArrayList<>();
		for (Entry<Integer, Integer> entry : _totalPoints.entrySet())
		{
			if (entry.getValue() > 0)
			{
				list.add(entry);
			}
		}
		
		// Optimization: Use cached total points and avoid redundant calculations during sorting.
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
