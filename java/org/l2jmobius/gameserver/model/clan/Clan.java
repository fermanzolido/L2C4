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
package org.l2jmobius.gameserver.model.clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.communitybbs.BB.Forum;
import org.l2jmobius.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.l2jmobius.gameserver.config.FeatureConfig;
import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.managers.CastleManager;
import org.l2jmobius.gameserver.managers.SiegeManager;
import org.l2jmobius.gameserver.model.BlockList;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.holders.actor.player.clan.OnPlayerClanJoin;
import org.l2jmobius.gameserver.model.events.holders.actor.player.clan.OnPlayerClanLeaderChange;
import org.l2jmobius.gameserver.model.events.holders.actor.player.clan.OnPlayerClanLeft;
import org.l2jmobius.gameserver.model.events.holders.actor.player.clan.OnPlayerClanLvlUp;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.itemcontainer.ClanWarehouse;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ServerPacket;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Clan
{
	private static final Logger LOGGER = Logger.getLogger(Clan.class.getName());
	
	// SQL queries
	private static final String INSERT_CLAN_DATA = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,blood_alliance_count,blood_oath_count,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,new_leader_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String SELECT_CLAN_DATA = "SELECT * FROM clan_data where clan_id=?";
	
	// Ally Penalty Types
	/** Clan leaved ally */
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	/** Clan was dismissed from ally */
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	/** Leader clan dismiss clan from ally */
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	/** Leader clan dissolve ally */
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	
	private String _name;
	private int _clanId;
	private ClanMember _leader;
	private final Map<Integer, ClanMember> _members = new ConcurrentHashMap<>();
	
	private String _allyName;
	private int _allyId = 0;
	private int _level;
	private int _castleId;
	private int _hideoutId;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	private int _bloodAllianceCount;
	private int _bloodOathCount;
	
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final Set<Integer> _atWarWith = ConcurrentHashMap.newKeySet();
	private final Set<Integer> _atWarAttackers = ConcurrentHashMap.newKeySet();
	
	private Forum _forum;
	
	private final Map<Integer, RankPrivs> _privs = new ConcurrentHashMap<>();
	private int _rank = 0;
	
	private String _notice;
	private boolean _noticeEnabled = false;
	private static final int MAX_NOTICE_LENGTH = 8192;
	private int _newLeaderId;
	
	private final AtomicInteger _siegeKills = new AtomicInteger();
	private final AtomicInteger _siegeDeaths = new AtomicInteger();
	
	/**
	 * Called if a clan is referenced only by id. In this case all other data needs to be fetched from db
	 * @param clanId A valid clan Id to create and restore
	 */
	public Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		restore();
		_warehouse.restore();
	}
	
	/**
	 * Called only if a new clan is created
	 * @param clanId A valid clan Id to create
	 * @param clanName A valid clan name
	 */
	public Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
	}
	
	/**
	 * @return Returns the clanId.
	 */
	public int getId()
	{
		return _clanId;
	}
	
	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return _leader != null ? _leader.getObjectId() : 0;
	}
	
	/**
	 * @return PledgeMember of clan leader.
	 */
	public ClanMember getLeader()
	{
		return _leader;
	}
	
	/**
	 * @param leader the leader to set.
	 */
	public void setLeader(ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getObjectId(), leader);
	}
	
	public void setNewLeader(ClanMember member)
	{
		final Player newLeader = member.getPlayer();
		final ClanMember exMember = _leader;
		final Player exLeader = exMember.getPlayer();
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEADER_CHANGE))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLeaderChange(exMember, member, this));
		}
		
		if (exLeader != null)
		{
			if (exLeader.isFlying())
			{
				exLeader.dismount();
			}
			
			if (getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().removeSiegeSkills(exLeader);
			}
			
			exLeader.getClanPrivileges().disableAll();
			exLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				ps.setInt(1, 0);
				ps.setInt(2, getLeaderId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Couldn't update clan privs for old clan leader", e);
			}
		}
		
		setLeader(member);
		if (_newLeaderId != 0)
		{
			setNewLeaderId(0, true);
		}
		
		updateClanInDB();
		
		if (exLeader != null)
		{
			exLeader.setPledgeClass(ClanMember.calculatePledgeClass(exLeader));
			exLeader.broadcastUserInfo();
			exLeader.checkItemRestriction();
		}
		
		if (newLeader != null)
		{
			newLeader.setPledgeClass(ClanMember.calculatePledgeClass(newLeader));
			newLeader.getClanPrivileges().enableAll();
			
			if (getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
			{
				SiegeManager.getInstance().addSiegeSkills(newLeader);
			}
			
			newLeader.broadcastUserInfo();
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?"))
			{
				ps.setInt(1, ClanPrivileges.getCompleteMask());
				ps.setInt(2, getLeaderId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Couldn't update clan privs for new clan leader", e);
			}
		}
		
		broadcastClanStatus();
		broadcastToOnlineMembers("Clan lord privileges have been transferred to " + member.getName() + ".");
		
		LOGGER.log(Level.INFO, "Leader of Clan: " + getName() + " changed to: " + member.getName() + " ex leader: " + exMember.getName());
	}
	
	/**
	 * @return the clan leader's name.
	 */
	public String getLeaderName()
	{
		if (_leader == null)
		{
			LOGGER.warning(Clan.class.getName() + ": Clan " + getName() + " without clan leader!");
			return "";
		}
		
		return _leader.getName();
	}
	
	/**
	 * @return the clan name.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * Adds a clan member to the clan.
	 * @param member the clan member.
	 */
	private void addClanMember(ClanMember member)
	{
		_members.put(member.getObjectId(), member);
	}
	
	/**
	 * Adds a clan member to the clan.<br>
	 * Using a different constructor, to make it easier to read.
	 * @param player the clan member
	 */
	public void addClanMember(Player player)
	{
		final ClanMember member = new ClanMember(this, player);
		member.setPlayer(player);
		addClanMember(member);
		
		player.setClan(this);
		player.setPledgeClass(ClanMember.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_JOIN))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanJoin(member, this));
		}
	}
	
	/**
	 * Updates player status in clan.
	 * @param player the player to be updated.
	 */
	public void updateClanMember(Player player)
	{
		final ClanMember member = new ClanMember(player.getClan(), player);
		if (player.isClanLeader())
		{
			setLeader(member);
		}
		
		addClanMember(member);
	}
	
	/**
	 * @param name the name of the required clan member.
	 * @return the clan member for a given name.
	 */
	public ClanMember getClanMember(String name)
	{
		for (ClanMember temp : _members.values())
		{
			if (temp.getName().equals(name))
			{
				return temp;
			}
		}
		
		return null;
	}
	
	/**
	 * @param objectId the required clan member object Id.
	 * @return the clan member for a given {@code objectId}.
	 */
	public ClanMember getClanMember(int objectId)
	{
		return _members.get(objectId);
	}
	
	/**
	 * @param objectId the object Id of the member that will be removed.
	 * @param clanJoinExpiryTime time penalty to join a clan.
	 */
	public void removeClanMember(int objectId, long clanJoinExpiryTime)
	{
		final ClanMember exMember = _members.remove(objectId);
		if (exMember == null)
		{
			LOGGER.warning("Member Object ID: " + objectId + " not found in clan while trying to remove");
			return;
		}
		
		if (PlayerConfig.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, getCastleId());
		}
		
		final Player player = exMember.getPlayer();
		if (player != null)
		{
			if (!player.isNoble())
			{
				player.setTitle("");
			}
			
			if (player.isClanLeader())
			{
				SiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(PlayerConfig.ALT_CLAN_CREATE_DAYS));
			}
			
			player.sendSkillList();
			player.setClan(null);
			
			// players leaving from clan academy have no penalty
			if (exMember.getPledgeType() != -1)
			{
				player.setClanJoinExpiryTime(clanJoinExpiryTime);
			}
			
			player.setPledgeClass(ClanMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			
			// disable clan tab
			player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
		}
		else
		{
			removeMemberInDatabase(exMember.getObjectId(), clanJoinExpiryTime, getLeaderId() == objectId ? System.currentTimeMillis() + TimeUnit.DAYS.toMillis(PlayerConfig.ALT_CLAN_CREATE_DAYS) : 0);
		}
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEFT))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLeft(exMember, this));
		}
	}
	
	public Collection<ClanMember> getMembers()
	{
		return _members.values();
	}
	
	public List<Integer> getOfflineMembersIds()
	{
		final List<Integer> list = new ArrayList<>();
		for (ClanMember temp : _members.values())
		{
			if ((temp != null) && !temp.isOnline())
			{
				list.add(temp.getObjectId());
			}
		}
		
		return list;
	}
	
	public int getMembersCount()
	{
		return _members.size();
	}
	
	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (ClanMember temp : _members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		
		return result;
	}
	
	/**
	 * @param pledgeType the Id of the pledge type.
	 * @return the maximum number of members allowed for a given {@code pledgeType}.
	 */
	public int getMaxNrOfMembers(int pledgeType)
	{
		int limit = 0;
		
		switch (pledgeType)
		{
			case 0:
			{
				switch (_level)
				{
					case 3:
					{
						limit = 30;
						break;
					}
					case 2:
					{
						limit = 20;
						break;
					}
					case 1:
					{
						limit = 15;
						break;
					}
					case 0:
					{
						limit = 10;
						break;
					}
					default:
					{
						limit = 40;
						break;
					}
				}
				break;
			}
			case -1:
			{
				limit = 20;
				break;
			}
			case 100:
			case 200:
			{
				switch (_level)
				{
					case 11:
					{
						limit = 30;
						break;
					}
					default:
					{
						limit = 20;
						break;
					}
				}
				break;
			}
			case 1001:
			case 1002:
			case 2001:
			case 2002:
			{
				switch (_level)
				{
					case 9:
					case 10:
					case 11:
					{
						limit = 25;
						break;
					}
					default:
					{
						limit = 10;
						break;
					}
				}
				break;
			}
			default:
			{
				break;
			}
		}
		
		return limit;
	}
	
	/**
	 * @param exclude the object Id to exclude from list.
	 * @return all online members excluding the one with object id {code exclude}.
	 */
	public List<Player> getOnlineMembers(int exclude)
	{
		final List<Player> onlineMembers = new ArrayList<>();
		for (ClanMember temp : _members.values())
		{
			if ((temp != null) && temp.isOnline() && (temp.getObjectId() != exclude))
			{
				onlineMembers.add(temp.getPlayer());
			}
		}
		
		return onlineMembers;
	}
	
	/**
	 * @return the online clan member count.
	 */
	public int getOnlineMembersCount()
	{
		int count = 0;
		for (ClanMember temp : _members.values())
		{
			if ((temp == null) || !temp.isOnline())
			{
				continue;
			}
			
			count++;
		}
		
		return count;
	}
	
	/**
	 * @return the alliance Id.
	 */
	public int getAllyId()
	{
		return _allyId;
	}
	
	/**
	 * @return the alliance name.
	 */
	public String getAllyName()
	{
		return _allyName;
	}
	
	/**
	 * @param allyCrestId the alliance crest Id to be set.
	 */
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}
	
	/**
	 * @return the alliance crest Id.
	 */
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	
	/**
	 * @return the clan level.
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * Sets the clan level and updates the clan forum if it's needed.
	 * @param level the clan level to be set.
	 */
	private void setLevel(int level)
	{
		_level = level;
		if ((_level >= 2) && (_forum == null) && GeneralConfig.ENABLE_COMMUNITY_BOARD)
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if (forum != null)
			{
				_forum = forum.getChildByName(_name);
				if (_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, getId());
				}
			}
		}
	}
	
	/**
	 * @return the castle Id for this clan if owns a castle, zero otherwise.
	 */
	public int getCastleId()
	{
		return _castleId;
	}
	
	/**
	 * @return the hideout Id for this clan if owns a hideout, zero otherwise.
	 */
	public int getHideoutId()
	{
		return _hideoutId;
	}
	
	/**
	 * @param crestId the Id of the clan crest to be set.
	 */
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	/**
	 * @return Returns the clanCrestId.
	 */
	public int getCrestId()
	{
		return _crestId;
	}
	
	/**
	 * @param crestLargeId The id of pledge LargeCrest.
	 */
	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}
	
	/**
	 * @return Returns the clan CrestLargeId
	 */
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}
	
	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	/**
	 * @param allyName The allyName to set.
	 */
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	/**
	 * @param castleId the castle Id to set.
	 */
	public void setCastleId(int castleId)
	{
		_castleId = castleId;
	}
	
	/**
	 * @param hideoutId the hideout Id to set.
	 */
	public void setHideoutId(int hideoutId)
	{
		_hideoutId = hideoutId;
	}
	
	/**
	 * @param id the Id of the player to be verified.
	 * @return {code true} if the player belongs to the clan.
	 */
	public boolean isMember(int id)
	{
		return (id != 0) && _members.containsKey(id);
	}
	
	/**
	 * @return the Blood Alliance count for this clan
	 */
	public int getBloodAllianceCount()
	{
		return _bloodAllianceCount;
	}
	
	/**
	 * Increase Blood Alliance count by config predefined count and updates the database.
	 */
	public void increaseBloodAllianceCount()
	{
		_bloodAllianceCount += SiegeManager.getInstance().getBloodAllianceReward();
		updateBloodAllianceCountInDB();
	}
	
	/**
	 * Reset the Blood Alliance count to zero and updates the database.
	 */
	public void resetBloodAllianceCount()
	{
		_bloodAllianceCount = 0;
		updateBloodAllianceCountInDB();
	}
	
	/**
	 * Store current Bloood Alliances count in database.
	 */
	public void updateBloodAllianceCountInDB()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_alliance_count=? WHERE clan_id=?"))
		{
			ps.setInt(1, _bloodAllianceCount);
			ps.setInt(2, _clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return the Blood Oath count for this clan
	 */
	public int getBloodOathCount()
	{
		return _bloodOathCount;
	}
	
	/**
	 * Increase Blood Oath count by config predefined count and updates the database.
	 */
	public void increaseBloodOathCount()
	{
		_bloodOathCount += FeatureConfig.FS_BLOOD_OATH_COUNT;
		updateBloodOathCountInDB();
	}
	
	/**
	 * Reset the Blood Oath count to zero and updates the database.
	 */
	public void resetBloodOathCount()
	{
		_bloodOathCount = 0;
		updateBloodOathCountInDB();
	}
	
	/**
	 * Store current Bloood Alliances count in database.
	 */
	public void updateBloodOathCountInDB()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET blood_oath_count=? WHERE clan_id=?"))
		{
			ps.setInt(1, _bloodOathCount);
			ps.setInt(2, _clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Updates in database clan information:
	 * <ul>
	 * <li>Clan leader Id</li>
	 * <li>Alliance Id</li>
	 * <li>Alliance name</li>
	 * <li>Clan's reputation</li>
	 * <li>Alliance's penalty expiration time</li>
	 * <li>Alliance's penalty type</li>
	 * <li>Character's penalty expiration time</li>
	 * <li>Dissolving expiration time</li>
	 * <li>Clan's id</li>
	 * </ul>
	 */
	public void updateClanInDB()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=?,new_leader_id=? WHERE clan_id=?"))
		{
			ps.setInt(1, getLeaderId());
			ps.setInt(2, _allyId);
			ps.setString(3, _allyName);
			ps.setInt(4, 0); // _reputationScore
			ps.setLong(5, _allyPenaltyExpiryTime);
			ps.setInt(6, _allyPenaltyType);
			ps.setLong(7, _charPenaltyExpiryTime);
			ps.setLong(8, _dissolvingExpiryTime);
			ps.setInt(9, _newLeaderId);
			ps.setInt(10, _clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error saving clan: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Stores in database clan information:
	 * <ul>
	 * <li>Clan Id</li>
	 * <li>Clan name</li>
	 * <li>Clan level</li>
	 * <li>Has castle</li>
	 * <li>Alliance Id</li>
	 * <li>Alliance name</li>
	 * <li>Clan leader Id</li>
	 * <li>Clan crest Id</li>
	 * <li>Clan large crest Id</li>
	 * <li>Alliance crest Id</li>
	 * </ul>
	 */
	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_CLAN_DATA))
		{
			ps.setInt(1, _clanId);
			ps.setString(2, _name);
			ps.setInt(3, _level);
			ps.setInt(4, _castleId);
			ps.setInt(5, _bloodAllianceCount);
			ps.setInt(6, _bloodOathCount);
			ps.setInt(7, _allyId);
			ps.setString(8, _allyName);
			ps.setInt(9, getLeaderId());
			ps.setInt(10, _crestId);
			ps.setInt(11, _crestLargeId);
			ps.setInt(12, _allyCrestId);
			ps.setInt(13, _newLeaderId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error saving new clan: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Removes a clan member from this clan.
	 * @param playerId the clan member object ID to be removed
	 * @param clanJoinExpiryTime the time penalty for the player to join a new clan
	 * @param clanCreateExpiryTime the time penalty for the player to create a new clan
	 */
	private void removeMemberInDatabase(int playerId, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?"))
		{
			// Remove clan member.
			ps1.setString(1, "");
			ps1.setLong(2, clanJoinExpiryTime);
			ps1.setLong(3, clanCreateExpiryTime);
			ps1.setInt(4, playerId);
			ps1.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error removing clan member: " + e.getMessage(), e);
		}
	}
	
	private void restore()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_CLAN_DATA))
		{
			ps.setInt(1, _clanId);
			try (ResultSet clanData = ps.executeQuery())
			{
				if (clanData.next())
				{
					setName(clanData.getString("clan_name"));
					setLevel(clanData.getInt("clan_level"));
					setCastleId(clanData.getInt("hasCastle"));
					_bloodAllianceCount = clanData.getInt("blood_alliance_count");
					_bloodOathCount = clanData.getInt("blood_oath_count");
					setAllyId(clanData.getInt("ally_id"));
					setAllyName(clanData.getString("ally_name"));
					setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
					if (_allyPenaltyExpiryTime < System.currentTimeMillis())
					{
						setAllyPenaltyExpiryTime(0, 0);
					}
					
					setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
					if ((_charPenaltyExpiryTime + (PlayerConfig.ALT_CLAN_JOIN_DAYS * 86400000)) < System.currentTimeMillis()) // 24*60*60*1000 = 86400000
					{
						setCharPenaltyExpiryTime(0);
					}
					
					setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
					
					setCrestId(clanData.getInt("crest_id"));
					setCrestLargeId(clanData.getInt("crest_large_id"));
					setAllyCrestId(clanData.getInt("ally_crest_id"));
					
					setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
					setNewLeaderId(clanData.getInt("new_leader_id"), false);
					
					final int leaderId = clanData.getInt("leader_id");
					ps.clearParameters();
					
					try (PreparedStatement select = con.prepareStatement("SELECT char_name,level,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"))
					{
						select.setInt(1, _clanId);
						try (ResultSet clanMember = select.executeQuery())
						{
							ClanMember member = null;
							while (clanMember.next())
							{
								member = new ClanMember(this, clanMember);
								if (member.getObjectId() == leaderId)
								{
									setLeader(member);
								}
								else
								{
									addClanMember(member);
								}
							}
						}
					}
				}
			}
			
			restoreRankPrivs();
			restoreNotice();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan data: " + e.getMessage(), e);
		}
	}
	
	private void restoreNotice()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?"))
		{
			ps.setInt(1, _clanId);
			try (ResultSet noticeData = ps.executeQuery())
			{
				while (noticeData.next())
				{
					_noticeEnabled = Boolean.parseBoolean(noticeData.getString("enabled"));
					_notice = noticeData.getString("notice");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan notice: " + e.getMessage(), e);
		}
	}
	
	private void storeNotice(String noticeValue, boolean enabled)
	{
		String notice = noticeValue;
		if (notice == null)
		{
			notice = "";
		}
		
		if (notice.length() > MAX_NOTICE_LENGTH)
		{
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO clan_notices (clan_id,notice,enabled) values (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?"))
		{
			ps.setInt(1, _clanId);
			ps.setString(2, notice);
			if (enabled)
			{
				ps.setString(3, "true");
			}
			else
			{
				ps.setString(3, "false");
			}
			
			ps.setString(4, notice);
			if (enabled)
			{
				ps.setString(5, "true");
			}
			else
			{
				ps.setString(5, "false");
			}
			
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error could not store clan notice: " + e.getMessage(), e);
		}
		
		_notice = notice;
		_noticeEnabled = enabled;
	}
	
	public void setNoticeEnabled(boolean enabled)
	{
		storeNotice(getNotice(), enabled);
	}
	
	public void setNotice(String notice)
	{
		storeNotice(notice, _noticeEnabled);
	}
	
	public boolean isNoticeEnabled()
	{
		return _noticeEnabled;
	}
	
	public String getNotice()
	{
		if (_notice == null)
		{
			return "";
		}
		
		// Bypass exploit check.
		final String text = _notice.toLowerCase();
		if (text.contains("action") && text.contains("bypass"))
		{
			return "";
		}
		
		// Returns text without tags.
		return _notice.replaceAll("<.*?>", "");
	}
	
	public void broadcastToOnlineAllyMembers(ServerPacket packet)
	{
		for (Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
		{
			clan.broadcastToOnlineMembers(packet);
		}
	}
	
	public void broadcastToOnlineMembers(ServerPacket packet)
	{
		for (ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline())
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOnlineMembers(String message)
	{
		for (ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline())
			{
				member.getPlayer().sendMessage(message);
			}
		}
	}
	
	public void broadcastCSToOnlineMembers(CreatureSay packet, Player broadcaster)
	{
		for (ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline() && !BlockList.isBlocked(member.getPlayer(), broadcaster))
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(ServerPacket packet, Player player)
	{
		for (ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline() && (member.getPlayer() != player))
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return _name + "[" + _clanId + "]";
	}
	
	public ItemContainer getWarehouse()
	{
		return _warehouse;
	}
	
	public boolean isAtWarWith(int id)
	{
		return _atWarWith.contains(id);
	}
	
	public boolean isAtWarWith(Clan clan)
	{
		return _atWarWith.contains(clan.getId());
	}
	
	public boolean isAtWarAttacker(int id)
	{
		return _atWarAttackers.contains(id);
	}
	
	public void setEnemyClan(Clan clan)
	{
		_atWarWith.add(clan.getId());
	}
	
	public void setEnemyClan(int id)
	{
		_atWarWith.add(id);
	}
	
	public void setAttackerClan(Clan clan)
	{
		_atWarAttackers.add(clan.getId());
	}
	
	public void setAttackerClan(int clan)
	{
		_atWarAttackers.add(clan);
	}
	
	public void deleteEnemyClan(Clan clan)
	{
		_atWarWith.remove(clan.getId());
	}
	
	public void deleteAttackerClan(Clan clan)
	{
		_atWarAttackers.remove(clan.getId());
	}
	
	public int getHiredGuards()
	{
		return _hiredGuards;
	}
	
	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}
	
	public boolean isAtWar()
	{
		return (_atWarWith != null) && !_atWarWith.isEmpty();
	}
	
	public Set<Integer> getWarList()
	{
		return _atWarWith;
	}
	
	public Set<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}
	
	public void broadcastClanStatus()
	{
		for (Player member : getOnlineMembers(0))
		{
			member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
			member.sendPacket(new PledgeShowMemberListAll(this, member));
		}
	}
	
	public static class RankPrivs
	{
		private final int _rankId;
		private final int _party; // Clan data fields (purpose unclear, retained for compatibility).
		private final ClanPrivileges _rankPrivs;
		
		public RankPrivs(int rank, int party, int privs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = new ClanPrivileges(privs);
		}
		
		public RankPrivs(int rank, int party, ClanPrivileges rankPrivs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = rankPrivs;
		}
		
		public int getRank()
		{
			return _rankId;
		}
		
		public int getParty()
		{
			return _party;
		}
		
		public ClanPrivileges getPrivs()
		{
			return _rankPrivs;
		}
		
		public void setPrivs(int privs)
		{
			_rankPrivs.setMask(privs);
		}
	}
	
	private void restoreRankPrivs()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT privs,`rank`,party FROM clan_privs WHERE clan_id=?"))
		{
			// Retrieve all skills of this Player from the database
			ps.setInt(1, _clanId);
			// LOGGER.warning("clanPrivs restore for ClanId : "+getClanId());
			try (ResultSet rset = ps.executeQuery())
			{
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					final int rank = rset.getInt("rank");
					
					// int party = rset.getInt("party");
					final int privileges = rset.getInt("privs");
					
					// Create a SubPledge object for each record
					if (rank == -1)
					{
						continue;
					}
					
					_privs.get(rank).setPrivs(privileges);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error restoring clan privs by rank: " + e.getMessage(), e);
		}
	}
	
	public void initializePrivs()
	{
		for (int i = 1; i < 10; i++)
		{
			_privs.put(i, new RankPrivs(i, 0, new ClanPrivileges()));
		}
	}
	
	public ClanPrivileges getRankPrivs(int rank)
	{
		return _privs.get(rank) != null ? _privs.get(rank).getPrivs() : new ClanPrivileges();
	}
	
	public void setRankPrivs(int rank, int privs)
	{
		if (_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)"))
			{
				// Retrieve all skills of this Player from the database
				ps.setInt(1, _clanId);
				ps.setInt(2, rank);
				ps.setInt(3, 0);
				ps.setInt(4, privs);
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Could not store clan privs for rank: " + e.getMessage(), e);
			}
			
			for (ClanMember cm : getMembers())
			{
				if (cm.isOnline() && (cm.getPowerGrade() == rank) && (cm.getPlayer() != null))
				{
					cm.getPlayer().getClanPrivileges().setMask(privs);
					cm.getPlayer().updateUserInfo();
				}
			}
			
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)"))
			{
				// Retrieve all skills of this Player from the database
				ps.setInt(1, _clanId);
				ps.setInt(2, rank);
				ps.setInt(3, 0);
				ps.setInt(4, privs);
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Could not create new rank and store clan privs for rank: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * @return all RankPrivs.
	 */
	public Collection<RankPrivs> getAllRankPrivs()
	{
		return _privs.values();
	}
	
	public synchronized void setRank(int rank)
	{
		_rank = rank;
	}
	
	public int getRank()
	{
		return _rank;
	}
	
	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}
	
	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?"))
			{
				ps.setInt(1, id);
				ps.setInt(2, _clanId);
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Could not store auction for clan: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * @param player the clan inviting player.
	 * @param target the invited player.
	 * @param pledgeType the pledge type to join.
	 * @return {core true} if player and target meet various conditions to join a clan.
	 */
	public boolean checkClanJoinCondition(Player player, Player target, int pledgeType)
	{
		if (player == null)
		{
			return false;
		}
		
		if (!player.hasAccess(ClanAccess.INVITE_MEMBER))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		
		if (target == null)
		{
			player.sendPacket(SystemMessageId.WRONG_TARGET_HAS_BEEN_INVITED);
			return false;
		}
		
		if (player.getObjectId() == target.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
			return false;
		}
		
		if (_charPenaltyExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.A_NEW_MEMBER_CANNOT_JOIN_WITHIN_5_DAYS_OF_A_CLAN_MEMBER_S_EXPULSION);
			return false;
		}
		
		if (target.getClanId() != 0)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_JOIN_THE_CLAN_BECAUSE_FIVE_DAYS_HAVE_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				player.sendMessage(_name + " is full and cannot accept additional clan members at this time.");
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_CLAN_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS);
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param player the clan inviting player.
	 * @param target the invited player.
	 * @return {core true} if player and target meet various conditions to join a clan.
	 */
	public boolean checkAllyJoinCondition(Player player, Player target)
	{
		if (player == null)
		{
			return false;
		}
		
		if ((player.getAllyId() == 0) || !player.isClanLeader() || (player.getClanId() != player.getAllyId()))
		{
			player.sendPacket(SystemMessageId.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
			return false;
		}
		
		final Clan leaderClan = player.getClan();
		if ((leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) && (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN))
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN);
			return false;
		}
		
		if (target == null)
		{
			player.sendPacket(SystemMessageId.WRONG_TARGET_HAS_BEEN_INVITED);
			return false;
		}
		
		if (player.getObjectId() == target.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
			return false;
		}
		
		if (target.getClan() == null)
		{
			player.sendPacket(SystemMessageId.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return false;
		}
		
		if (!target.isClanLeader())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		final Clan targetClan = target.getClan();
		if (target.getAllyId() != 0)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			player.sendPacket(sm);
			return false;
		}
		
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE);
				sm.addString(target.getClan().getName());
				sm.addString(target.getClan().getAllyName());
				player.sendPacket(sm);
				return false;
			}
			
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				player.sendPacket(SystemMessageId.A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION);
				return false;
			}
		}
		
		if (player.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
			return false;
		}
		
		if (leaderClan.isAtWarWith(targetClan.getId()))
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_AT_BATTLE_WITH);
			return false;
		}
		
		if (ClanTable.getInstance().getClanAllies(player.getAllyId()).size() >= PlayerConfig.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}
		
		return true;
	}
	
	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}
	
	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}
	
	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}
	
	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}
	
	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}
	
	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}
	
	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}
	
	public void createAlly(Player player, String allyName)
	{
		if (null == player)
		{
			return;
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			return;
		}
		
		if (_allyId != 0)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_JOINED_AN_ALLIANCE);
			return;
		}
		
		if (_level < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		
		if ((_allyPenaltyExpiryTime > System.currentTimeMillis()) && (_allyPenaltyType == PENALTY_TYPE_DISSOLVE_ALLY))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION);
			return;
		}
		
		if (_dissolvingExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT);
			return;
		}
		
		if (!StringUtil.isAlphaNumeric(allyName))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
			return;
		}
		
		if ((allyName.length() > 16) || (allyName.length() < 2))
		{
			player.sendPacket(SystemMessageId.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			return;
		}
		
		if (ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.THE_FOLLOWING_ALLIANCE_ALREADY_EXISTS);
			return;
		}
		
		setAllyId(_clanId);
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();
		
		player.updateUserInfo();
		
		// Uses approximate message ID.
		player.sendMessage("Alliance " + allyName + " has been created.");
	}
	
	public void dissolveAlly(Player player)
	{
		if (_allyId == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		
		if (!player.isClanLeader() || (_clanId != _allyId))
		{
			player.sendPacket(SystemMessageId.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
			return;
		}
		
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
			return;
		}
		
		broadcastToOnlineAllyMembers(new SystemMessage(SystemMessageId.THE_ALLIANCE_HAS_DISSOLVED));
		
		final long currentTime = System.currentTimeMillis();
		for (Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
		{
			if (clan.getId() != getId())
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}
		}
		
		setAllyId(0);
		setAllyName(null);
		changeAllyCrest(0, false);
		setAllyPenaltyExpiryTime(currentTime + (PlayerConfig.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000), PENALTY_TYPE_DISSOLVE_ALLY); // 24*60*60*1000 = 86400000
		updateClanInDB();
	}
	
	public boolean levelUpClan(Player player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		
		if (System.currentTimeMillis() < _dissolvingExpiryTime)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT);
			return false;
		}
		
		boolean increaseClanLevel = false;
		
		switch (_level)
		{
			case 0:
			{
				// Upgrade to 1
				if ((player.getSp() >= 20000) && (player.getAdena() >= 650000) && player.reduceAdena(ItemProcessType.FEE, 650000, player.getTarget(), true))
				{
					player.setSp(player.getSp() - 20000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.SP_HAS_DECREASED_BY_S1);
					sp.addInt(20000);
					player.sendPacket(sp);
					increaseClanLevel = true;
				}
				break;
			}
			case 1:
			{
				// Upgrade to 2
				if ((player.getSp() >= 100000) && (player.getAdena() >= 2500000) && player.reduceAdena(ItemProcessType.FEE, 2500000, player.getTarget(), true))
				{
					player.setSp(player.getSp() - 100000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.SP_HAS_DECREASED_BY_S1);
					sp.addInt(100000);
					player.sendPacket(sp);
					increaseClanLevel = true;
				}
				break;
			}
			case 2:
			{
				// Upgrade to 3
				if ((player.getSp() >= 350000) && (player.getInventory().getItemByItemId(1419) != null) && player.destroyItemByItemId(ItemProcessType.FEE, 1419, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 350000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.SP_HAS_DECREASED_BY_S1);
					sp.addInt(350000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
					sm.addItemName(1419);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			case 3:
			{
				// Upgrade to 4 (itemId 3874 = Alliance Manifesto)
				if ((player.getSp() >= 1000000) && (player.getInventory().getItemByItemId(3874) != null) && player.destroyItemByItemId(ItemProcessType.FEE, 3874, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 1000000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.SP_HAS_DECREASED_BY_S1);
					sp.addInt(1000000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
					sm.addItemName(3874);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			case 4:
			{
				// Upgrade to 5 (itemId 3870 = Seal of Aspiration)
				if ((player.getSp() >= 2500000) && (player.getInventory().getItemByItemId(3870) != null) && player.destroyItemByItemId(ItemProcessType.FEE, 3870, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 2500000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.SP_HAS_DECREASED_BY_S1);
					sp.addInt(2500000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
					sm.addItemName(3870);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			default:
			{
				return false;
			}
		}
		
		if (!increaseClanLevel)
		{
			player.sendMessage("The conditions necessary to increase the clan's level have not been met.");
			return false;
		}
		
		// the player should know that he has less sp now :p
		final StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.SP, (int) player.getSp());
		player.sendPacket(su);
		
		player.sendItemList(false);
		changeLevel(_level + 1);
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_LEVELUP))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanLvlUp(player, this));
		}
		
		return true;
	}
	
	public void changeLevel(int level)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?"))
		{
			ps.setInt(1, level);
			ps.setInt(2, _clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "could not increase clan level:" + e.getMessage(), e);
		}
		
		setLevel(level);
		setRank(ClanTable.getInstance().getClanRank(this));
		
		if (_leader.isOnline())
		{
			final Player leader = _leader.getPlayer();
			if (level > 4)
			{
				SiegeManager.getInstance().addSiegeSkills(leader);
				leader.sendMessage("Now that your clan level is above Level 5, it can accumulate clan reputation points.");
			}
			else if (level < 5)
			{
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
		}
		
		// notify all the members about it
		broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_S_SKILL_LEVEL_HAS_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}
	
	/**
	 * Change the clan crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeClanCrest(int crestId)
	{
		if (_crestId != 0)
		{
			CrestTable.getInstance().removeCrest(getCrestId());
		}
		
		setCrestId(crestId);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?"))
		{
			ps.setInt(1, crestId);
			ps.setInt(2, _clanId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Could not update crest for clan " + _name + " [" + _clanId + "] : " + e.getMessage(), e);
		}
		
		for (Player member : getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	/**
	 * Change the ally crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 * @param onlyThisClan
	 */
	public void changeAllyCrest(int crestId, boolean onlyThisClan)
	{
		String sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
		int allyId = _clanId;
		if (!onlyThisClan)
		{
			if (_allyCrestId != 0)
			{
				CrestTable.getInstance().removeCrest(getAllyCrestId());
			}
			
			sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
			allyId = _allyId;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(sqlStatement))
		{
			ps.setInt(1, crestId);
			ps.setInt(2, allyId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Could not update ally crest for ally/clan id " + allyId + " : " + e.getMessage(), e);
		}
		
		if (onlyThisClan)
		{
			setAllyCrestId(crestId);
			for (Player member : getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
		else
		{
			for (Clan clan : ClanTable.getInstance().getClanAllies(getAllyId()))
			{
				clan.setAllyCrestId(crestId);
				for (Player member : clan.getOnlineMembers(0))
				{
					member.broadcastUserInfo();
				}
			}
		}
	}
	
	/**
	 * Change the large crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeLargeCrest(int crestId)
	{
		if (_crestLargeId != 0)
		{
			CrestTable.getInstance().removeCrest(getCrestLargeId());
		}
		
		setCrestLargeId(crestId);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?"))
		{
			ps.setInt(1, crestId);
			ps.setInt(2, _clanId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Could not update large crest for clan " + _name + " [" + _clanId + "] : " + e.getMessage(), e);
		}
		
		for (Player member : getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	public void setNewLeaderId(int objectId, boolean storeInDb)
	{
		_newLeaderId = objectId;
		if (storeInDb)
		{
			updateClanInDB();
		}
	}
	
	public int getNewLeaderId()
	{
		return _newLeaderId;
	}
	
	public Player getNewLeader()
	{
		return World.getInstance().getPlayer(_newLeaderId);
	}
	
	public String getNewLeaderName()
	{
		return CharInfoTable.getInstance().getNameById(_newLeaderId);
	}
	
	public int getSiegeKills()
	{
		return _siegeKills.get();
	}
	
	public int getSiegeDeaths()
	{
		return _siegeDeaths.get();
	}
	
	public int addSiegeKill()
	{
		return _siegeKills.incrementAndGet();
	}
	
	public int addSiegeDeath()
	{
		return _siegeDeaths.incrementAndGet();
	}
	
	public void clearSiegeKills()
	{
		_siegeKills.set(0);
	}
	
	public void clearSiegeDeaths()
	{
		_siegeDeaths.set(0);
	}
}
