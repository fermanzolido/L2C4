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
	private final Map<Integer, Integer> _totalPoints = new ConcurrentHashMap<>();
	
	protected RaidBossPointsManager()
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
				
				_list.computeIfAbsent(charId, unused -> new ConcurrentHashMap<>()).put(bossId, points);
				_totalPoints.merge(charId, points, Integer::sum);
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _list.size() + " Characters Raid Points.");
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not load raid points ", e);
		}
	}
	
	/**
	 * Updates the raid points for multiple players in the database using batching.
	 * @param playerPoints a map of players and their new points for the given raid ID
	 * @param raidId the ID of the raid boss
	 */
	public void updatePointsInDB(Map<Player, Integer> playerPoints, int raidId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)"))
		{
			for (Entry<Player, Integer> entry : playerPoints.entrySet())
			{
				ps.setInt(1, entry.getKey().getObjectId());
				ps.setInt(2, raidId);
				ps.setInt(3, entry.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update char raid points batch for raid: " + raidId, e);
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
	
	/**
	 * Adds raid points to multiple players and updates the database.
	 * @param playerPoints a map containing players and the points to be added
	 * @param bossId the ID of the raid boss
	 */
	public void addPoints(Map<Player, Integer> playerPoints, int bossId)
	{
		final Map<Player, Integer> newPoints = new HashMap<>();
		for (Entry<Player, Integer> entry : playerPoints.entrySet())
		{
			final Player player = entry.getKey();
			final int pointsToAdd = entry.getValue();
			final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(player.getObjectId(), unused -> new ConcurrentHashMap<>());
			final int totalRaidPoints = tmpPoint.merge(bossId, pointsToAdd, Integer::sum);
			_totalPoints.merge(player.getObjectId(), pointsToAdd, Integer::sum);
			newPoints.put(player, totalRaidPoints);
		}
		updatePointsInDB(newPoints, bossId);
	}

	public void addPoints(Player player, int bossId, int points)
	{
		final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(player.getObjectId(), unused -> new ConcurrentHashMap<>());
		final int totalRaidPoints = tmpPoint.merge(bossId, points, Integer::sum);
		_totalPoints.merge(player.getObjectId(), points, Integer::sum);
		updatePointsInDB(player, bossId, totalRaidPoints);
	}
	
	/**
	 * Gets the total raid points for a given character ID.
	 * @param ownerId the character ID
	 * @return the total points (O(1) lookup)
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
	 * Calculates the ranking for a player based on their total raid points.
	 * Uses standard competition ranking: O(N) complexity.
	 * @param playerObjId the player object ID
	 * @return the rank, or 0 if no points
	 */
	public int calculateRanking(int playerObjId)
	{
		final int totalPoints = getPointsByOwnerId(playerObjId);
		if (totalPoints == 0)
		{
			return 0;
		}
		
		int rank = 1;
		for (int points : _totalPoints.values())
		{
			if (points > totalPoints)
			{
				rank++;
			}
		}
		return rank;
	}

	/**
	 * Gets a map of character IDs and their ranks using standard competition ranking.
	 * @return a map of charId -> rank
	 */
	public Map<Integer, Integer> getRankList()
	{
		final List<Entry<Integer, Integer>> list = new ArrayList<>(_totalPoints.entrySet());
		list.sort(Comparator.comparing(Entry<Integer, Integer>::getValue).reversed());

		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		int rank = 1;
		int lastPoints = -1;
		int playersAtRank = 0;
		for (Entry<Integer, Integer> entry : list)
		{
			final int currentPoints = entry.getValue();
			if (currentPoints == 0)
			{
				continue;
			}

			if (currentPoints != lastPoints)
			{
				rank += playersAtRank;
				playersAtRank = 1;
				lastPoints = currentPoints;
			}
			else
			{
				playersAtRank++;
			}

			tmpRanking.put(entry.getKey(), rank);
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
