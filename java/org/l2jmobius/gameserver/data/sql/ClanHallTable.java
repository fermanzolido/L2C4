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
package org.l2jmobius.gameserver.data.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.managers.CHSiegeManager;
import org.l2jmobius.gameserver.managers.ClanHallAuctionManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.residences.AuctionableHall;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.model.siege.clanhalls.SiegableHall;
import org.l2jmobius.gameserver.model.zone.type.ClanHallZone;

/**
 * @author Steuf
 */
public class ClanHallTable
{
	protected static final Logger LOGGER = Logger.getLogger(ClanHallTable.class.getName());
	
	private final Map<Integer, AuctionableHall> _clanHall = new ConcurrentHashMap<>();
	private final Map<Integer, AuctionableHall> _freeClanHall = new ConcurrentHashMap<>();
	private final Map<Integer, AuctionableHall> _allAuctionableClanHalls = new HashMap<>();
	private static Map<Integer, ClanHall> _allClanHalls = new HashMap<>();
	private boolean _loaded = false;
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	protected ClanHallTable()
	{
		load();
	}
	
	/** Load All Clan Hall */
	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM clanhall ORDER BY id"))
		{
			int id;
			int ownerId;
			int lease;
			while (rs.next())
			{
				final StatSet set = new StatSet();
				id = rs.getInt("id");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", ownerId);
				set.set("lease", lease);
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("paidUntil", rs.getLong("paidUntil"));
				set.set("grade", rs.getInt("Grade"));
				set.set("paid", rs.getBoolean("paid"));
				final AuctionableHall ch = new AuctionableHall(set);
				_allAuctionableClanHalls.put(id, ch);
				addClanHall(ch);
				
				if (ch.getOwnerId() > 0)
				{
					_clanHall.put(id, ch);
					continue;
				}
				
				_freeClanHall.put(id, ch);
				
				if ((ClanHallAuctionManager.getInstance().getAuction(id) == null) && (lease > 0))
				{
					ClanHallAuctionManager.getInstance().initNPC(id);
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _clanHall.size() + " clan halls");
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _freeClanHall.size() + " free clan halls");
			_loaded = true;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception: ClanHallManager.load(): " + e.getMessage(), e);
		}
	}
	
	public static Map<Integer, ClanHall> getAllClanHalls()
	{
		return _allClanHalls;
	}
	
	/**
	 * @return all FreeClanHalls
	 */
	public Map<Integer, AuctionableHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	/**
	 * @return all ClanHalls that have owner
	 */
	public Map<Integer, AuctionableHall> getClanHalls()
	{
		return _clanHall;
	}
	
	/**
	 * @return all ClanHalls
	 */
	public Map<Integer, AuctionableHall> getAllAuctionableClanHalls()
	{
		return _allAuctionableClanHalls;
	}
	
	public static void addClanHall(ClanHall hall)
	{
		_allClanHalls.put(hall.getId(), hall);
	}
	
	/**
	 * @param chId
	 * @return true is free ClanHall
	 */
	public boolean isFree(int chId)
	{
		return _freeClanHall.containsKey(chId);
	}
	
	/**
	 * Free a ClanHall
	 * @param chId
	 */
	public synchronized void setFree(int chId)
	{
		// A hall that is not owned is not in _clanHall, and putting the null that get returns
		// into a ConcurrentHashMap throws. Reachable from //siege on a hall that is already
		// free.
		final AuctionableHall hall = _clanHall.get(chId);
		if (hall == null)
		{
			return;
		}
		
		_freeClanHall.put(chId, hall);
		
		// The hall records an owner id, which can outlive the clan itself; getClan()
		// returns null for a clan that is gone. Freeing the hall must not depend on the
		// owner still being there.
		final Clan owner = ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId());
		if (owner != null)
		{
			owner.setHideoutId(0);
		}
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/**
	 * Set ClanHallOwner
	 * @param chId
	 * @param clan
	 */
	public synchronized void setOwner(int chId, Clan clan)
	{
		AuctionableHall hall = _clanHall.get(chId);
		if (hall == null)
		{
			// Same null as setFree: a hall in neither map cannot be handed over, and moving it
			// between the maps first meant the throw landed with the hall already relocated.
			hall = _freeClanHall.get(chId);
			if (hall == null)
			{
				return;
			}
			
			_clanHall.put(chId, hall);
			_freeClanHall.remove(chId);
		}
		else
		{
			hall.free();
		}
		
		// The clan is already in hand. Looking it up again by id and dereferencing the answer
		// meant a clan disbanded between winning the auction and the auction closing threw
		// here, after the hall had been moved.
		clan.setHideoutId(chId);
		hall.setOwner(clan);
	}
	
	/**
	 * @param clanHallId
	 * @return Clan Hall by Id
	 */
	public ClanHall getClanHallById(int clanHallId)
	{
		return _allClanHalls.get(clanHallId);
	}
	
	public AuctionableHall getAuctionableHallById(int clanHallId)
	{
		return _allAuctionableClanHalls.get(clanHallId);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return Clan Hall by x,y,z
	 */
	public ClanHall getClanHall(int x, int y, int z)
	{
		for (ClanHall temp : _allClanHalls.values())
		{
			if (temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public ClanHall getClanHall(WorldObject activeObject)
	{
		return getClanHall(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public AuctionableHall getNearbyClanHall(int x, int y, int maxDist)
	{
		ClanHallZone zone = null;
		for (Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		
		for (Entry<Integer, AuctionableHall> ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		
		return null;
	}
	
	public ClanHall getNearbyAbstractHall(int x, int y, int maxDist)
	{
		ClanHallZone zone = null;
		for (Entry<Integer, ClanHall> ch : _allClanHalls.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		
		return null;
	}
	
	/**
	 * @param clan
	 * @return Clan Hall by Owner
	 */
	public AuctionableHall getClanHallByOwner(Clan clan)
	{
		for (Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		
		return null;
	}
	
	public ClanHall getAbstractHallByOwner(Clan clan)
	{
		// Separate loops to avoid iterating over free clan halls
		for (Entry<Integer, AuctionableHall> ch : _clanHall.entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		
		for (Entry<Integer, SiegableHall> ch : CHSiegeManager.getInstance().getConquerableHalls().entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		
		return null;
	}
	
	public static ClanHallTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallTable INSTANCE = new ClanHallTable();
	}
}
