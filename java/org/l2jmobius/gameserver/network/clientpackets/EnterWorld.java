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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.config.RatesConfig;
import org.l2jmobius.gameserver.config.ServerConfig;
import org.l2jmobius.gameserver.config.custom.FactionSystemConfig;
import org.l2jmobius.gameserver.config.custom.OfflineTradeConfig;
import org.l2jmobius.gameserver.config.custom.PremiumSystemConfig;
import org.l2jmobius.gameserver.config.custom.ScreenWelcomeMessageConfig;
import org.l2jmobius.gameserver.config.custom.WeddingConfig;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.EnchantItemGroupsData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.managers.AntiFeedManager;
import org.l2jmobius.gameserver.managers.CHSiegeManager;
import org.l2jmobius.gameserver.managers.CoupleManager;
import org.l2jmobius.gameserver.managers.DimensionalRiftManager;
import org.l2jmobius.gameserver.managers.InstanceManager;
import org.l2jmobius.gameserver.managers.PcCafePointsManager;
import org.l2jmobius.gameserver.managers.PunishmentManager;
import org.l2jmobius.gameserver.managers.ServerRestartManager;
import org.l2jmobius.gameserver.managers.SiegeManager;
import org.l2jmobius.gameserver.model.Couple;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.appearance.PlayerAppearance;
import org.l2jmobius.gameserver.model.actor.enums.creature.Race;
import org.l2jmobius.gameserver.model.actor.enums.player.IllegalActionPunishmentType;
import org.l2jmobius.gameserver.model.actor.enums.player.TeleportWhereType;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.punishment.PunishmentAffect;
import org.l2jmobius.gameserver.model.punishment.PunishmentType;
import org.l2jmobius.gameserver.model.residences.AuctionableHall;
import org.l2jmobius.gameserver.model.script.Quest;
import org.l2jmobius.gameserver.model.script.QuestState;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.siege.Siege;
import org.l2jmobius.gameserver.model.siege.clanhalls.SiegableHall;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.holders.ClientHardwareInfoHolder;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.Die;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.network.serverpackets.FriendList;
import org.l2jmobius.gameserver.network.serverpackets.HennaInfo;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeStatusChanged;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.ShortcutInit;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;
import org.l2jmobius.gameserver.network.serverpackets.StartRotation;
import org.l2jmobius.gameserver.network.serverpackets.StopRotation;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import org.l2jmobius.gameserver.taskmanagers.GameTimeTaskManager;

public class EnterWorld extends ClientPacket
{
	private static final Map<String, ClientHardwareInfoHolder> TRACE_HWINFO = new ConcurrentHashMap<>();
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		final Player player = client.getPlayer();
		if (player == null)
		{
			PacketLogger.warning("EnterWorld failed! player returned 'null'.");
			Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			return;
		}
		
		client.setConnectionState(ConnectionState.IN_GAME);
		
		player.sendPacket(new UserInfo(player));
		
		// Restore to instanced area if enabled
		if (GeneralConfig.RESTORE_PLAYER_INSTANCE)
		{
			player.setInstanceId(InstanceManager.getInstance().getPlayer(player.getObjectId()));
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().getPlayer(player.getObjectId());
			if (instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(player.getObjectId());
			}
		}
		
		if (!player.isGM())
		{
			player.updatePvpTitleAndColor(false);
		}
		
		// Apply special GM properties to the GM when entering
		else
		{
			gmStartupProcess:
			{
				if (GeneralConfig.GM_STARTUP_BUILDER_HIDE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				{
					player.setHiding(true);
					player.sendSysMessage("hide is default for builder.");
					player.sendSysMessage("FriendAddOff is default for builder.");
					player.sendSysMessage("whisperoff is default for builder.");
					
					// It isn't recommend to use the below custom L2J GMStartup functions together with retail-like GMStartupBuilderHide, so breaking the process at that stage.
					break gmStartupProcess;
				}
				
				if (GeneralConfig.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
				{
					player.setInvul(true);
				}
				
				if (GeneralConfig.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
				{
					player.setInvisible(true);
				}
				
				if (GeneralConfig.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
				{
					player.setSilenceMode(true);
				}
				
				if (GeneralConfig.GM_STARTUP_DIET_MODE && AdminData.getInstance().hasAccess("admin_diet", player.getAccessLevel()))
				{
					player.setDietMode(true);
					player.refreshOverloaded();
				}
			}
			
			if (GeneralConfig.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
			{
				AdminData.getInstance().addGm(player, false);
			}
			else
			{
				AdminData.getInstance().addGm(player, true);
			}
			
			if (GeneralConfig.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, false);
			}
			
			if (GeneralConfig.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, true);
			}
		}
		
		// Set dead status if applies
		if (player.getCurrentHp() < 0.5)
		{
			player.setDead(true);
		}
		
		boolean showClanNotice = false;
		
		// Clan related checks are here
		final Clan clan = player.getClan();
		if (clan != null)
		{
			// player.sendPacket(new PledgeSkillList(clan));
			notifyClanMembers(player);
			
			final AuctionableHall clanHall = ClanHallTable.getInstance().getClanHallByOwner(clan);
			if ((clanHall != null) && !clanHall.getPaid())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				sm.addInt(clanHall.getLease());
				player.sendPacket(sm);
			}
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
				else if (siege.checkIsDefender(clan))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
			}
			
			for (SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				if (!hall.isInSiege())
				{
					continue;
				}
				
				if (hall.isRegistered(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(hall.getId());
					player.setInHideoutSiege(true);
				}
			}
			
			player.sendPacket(new PledgeShowMemberListAll(clan, player));
			player.sendPacket(new PledgeStatusChanged(clan));
			showClanNotice = clan.isNoticeEnabled();
		}
		
		if (PlayerConfig.ENABLE_VITALITY && PlayerConfig.RECOVER_VITALITY_ON_RECONNECT)
		{
			final float points = (RatesConfig.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - player.getLastAccess())) / 60000;
			if (points > 0)
			{
				player.updateVitalityPoints(points, false, true);
			}
		}
		
		// Send Macro List
		player.getMacros().sendUpdate();
		
		// Send Item List
		player.sendPacket(new ItemList(player, false));
		
		// Send Shortcuts
		player.sendPacket(new ShortcutInit(player));
		
		// Send Dye Information
		player.sendPacket(new HennaInfo(player));
		Quest.playerEnter(player);
		
		// Faction System
		if (FactionSystemConfig.FACTION_SYSTEM_ENABLED)
		{
			final PlayerAppearance appearance = player.getAppearance();
			if (player.isGood())
			{
				appearance.setNameColor(FactionSystemConfig.FACTION_GOOD_NAME_COLOR);
				appearance.setTitleColor(FactionSystemConfig.FACTION_GOOD_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_GOOD_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_GOOD_TEAM_NAME + " faction.", 10000));
				player.updateUserInfo(); // for seeing self name color
			}
			else if (player.isEvil())
			{
				appearance.setNameColor(FactionSystemConfig.FACTION_EVIL_NAME_COLOR);
				appearance.setTitleColor(FactionSystemConfig.FACTION_EVIL_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_EVIL_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + FactionSystemConfig.FACTION_EVIL_TEAM_NAME + " faction.", 10000));
				player.updateUserInfo(); // for seeing self name color
			}
		}
		
		if (!PlayerConfig.DISABLE_TUTORIAL)
		{
			loadTutorial(player);
		}
		
		player.sendPacket(new QuestList(player));
		if (PlayerConfig.PLAYER_SPAWN_PROTECTION > 0)
		{
			player.setSpawnProtection(true);
		}
		
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		player.sendPacket(new StartRotation(player.getObjectId(), player.getHeading()));
		player.sendPacket(new StopRotation(player.getObjectId(), player.getHeading()));
		
		player.updateEffectIcons();
		
		player.sendPacket(new EtcStatusUpdate(player));
		
		// Expand Skill
		player.sendPacket(new ExStorageMaxCount(player));
		player.sendPacket(new FriendList(player));
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_FRIEND_HAS_LOGGED_IN);
		sm.addString(player.getName());
		for (int id : player.getFriendList())
		{
			final WorldObject obj = World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		player.sendPacket(SystemMessageId.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(player);
		AnnouncementsTable.getInstance().showAnnouncements(player);
		
		if ((ServerConfig.SERVER_RESTART_SCHEDULE_ENABLED) && (ServerConfig.SERVER_RESTART_SCHEDULE_MESSAGE))
		{
			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, "[SERVER]", "Next restart is scheduled at " + ServerRestartManager.getInstance().getNextRestartTime() + "."));
		}
		
		if (showClanNotice)
		{
			final NpcHtmlMessage notice = new NpcHtmlMessage();
			notice.setFile(player, "data/html/clanNotice.htm");
			notice.replace("%clan_name%", player.getClan().getName());
			notice.replace("%notice_text%", player.getClan().getNotice().replaceAll("(\r\n|\n)", "<br>"));
			notice.disableValidation();
			player.sendPacket(notice);
		}
		else if (GeneralConfig.SERVER_NEWS)
		{
			final String serverNews = HtmCache.getInstance().getHtm(player, "data/html/servnews.htm");
			if (serverNews != null)
			{
				player.sendPacket(new NpcHtmlMessage(serverNews));
			}
		}
		
		if (player.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			player.sendPacket(new Die(player));
		}
		
		player.onPlayerEnter();
		
		// Apply item skills.
		player.getInventory().applyItemSkills();
		
		// Send Skill list
		player.sendSkillList();
		
		// Remain in party after logout fix.
		player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
		
		player.sendPacket(new SkillCoolTime(player));
		for (Item i : player.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			
			if (i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}
		
		for (Item i : player.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		}
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DISMISSED_FROM_THIS_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_THE_NEXT_FIVE_DAYS);
		}
		
		// Attacker or spectator logging in to a siege zone.
		// Actually should be checked for inside castle only?
		if (!player.isGM() && player.isInsideZone(ZoneId.SIEGE) && (!player.isInSiege() || (player.getSiegeState() < 2)))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}
		
		// Over-enchant protection.
		if (PlayerConfig.OVER_ENCHANT_PROTECTION && !player.isGM())
		{
			boolean punish = false;
			for (Item item : player.getInventory().getItems())
			{
				if (item.isEquipable() //
					&& ((item.isWeapon() && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxWeaponEnchant())) //
						|| ((item.getTemplate().getType2() == ItemTemplate.TYPE2_ACCESSORY) && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxAccessoryEnchant())) //
						|| (item.isArmor() && (item.getTemplate().getType2() != ItemTemplate.TYPE2_ACCESSORY) && (item.getEnchantLevel() > EnchantItemGroupsData.getInstance().getMaxArmorEnchant()))))
				{
					PacketLogger.info("Over-enchanted (+" + item.getEnchantLevel() + ") " + item + " has been removed from " + player);
					player.getInventory().destroyItem(ItemProcessType.DESTROY, item, player, null);
					punish = true;
				}
			}
			
			if (punish && (PlayerConfig.OVER_ENCHANT_PUNISHMENT != IllegalActionPunishmentType.NONE))
			{
				player.sendMessage("[Server]: You have over-enchanted items!");
				player.sendMessage("[Server]: Respect our server rules.");
				player.sendPacket(new ExShowScreenMessage("You have over-enchanted items!", 6000));
				PunishmentManager.handleIllegalPlayerAction(player, player.getName() + " has over-enchanted items.", PlayerConfig.OVER_ENCHANT_PUNISHMENT);
			}
		}
		
		if (ScreenWelcomeMessageConfig.WELCOME_MESSAGE_ENABLED)
		{
			player.sendPacket(new ExShowScreenMessage(ScreenWelcomeMessageConfig.WELCOME_MESSAGE_TEXT, ScreenWelcomeMessageConfig.WELCOME_MESSAGE_TIME));
		}
		
		if ((OfflineTradeConfig.OFFLINE_TRADE_ENABLE || OfflineTradeConfig.OFFLINE_CRAFT_ENABLE) && OfflineTradeConfig.STORE_OFFLINE_TRADE_IN_REALTIME)
		{
			OfflineTraderTable.getInstance().onTransaction(player, true, false);
		}
		
		// Check if expoff is enabled.
		if (player.getVariables().getBoolean("EXPOFF", false))
		{
			player.disableExpGain();
			player.sendMessage("Experience gain is disabled.");
		}
		
		player.broadcastUserInfo();
		
		// Prevent relogin in game gfx.
		player.sendPacket(new ValidateLocation(player));
		
		// Unstuck players that had client open when server crashed.
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		// Delayed HWID checks.
		if (ServerConfig.HARDWARE_INFO_ENABLED)
		{
			ThreadPool.schedule(() ->
			{
				// Use client IP as identifier.
				final String ip = client.getIp();
				
				// Get hardware info from client.
				ClientHardwareInfoHolder hwInfo = client.getHardwareInfo();
				if (hwInfo != null)
				{
					hwInfo.store(player);
					TRACE_HWINFO.put(ip, hwInfo);
				}
				else
				{
					// Get hardware info from stored IP map.
					hwInfo = TRACE_HWINFO.get(ip);
					if (hwInfo != null)
					{
						hwInfo.store(player);
						client.setHardwareInfo(hwInfo);
					}
					// Get hardware info from account variables.
					else
					{
						final String storedInfo = player.getAccountVariables().getString(AccountVariables.HWID, "");
						if (!storedInfo.isEmpty())
						{
							hwInfo = new ClientHardwareInfoHolder(storedInfo);
							TRACE_HWINFO.put(ip, hwInfo);
							client.setHardwareInfo(hwInfo);
						}
					}
				}
				
				// Banned?
				if ((hwInfo != null) && PunishmentManager.getInstance().hasPunishment(hwInfo.getMacAddress(), PunishmentAffect.HWID, PunishmentType.BAN))
				{
					Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
					return;
				}
				
				// Check max players.
				if (ServerConfig.KICK_MISSING_HWID && (hwInfo == null))
				{
					Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
				}
				else if (ServerConfig.MAX_PLAYERS_PER_HWID > 0)
				{
					int count = 0;
					for (Player plr : World.getInstance().getPlayers())
					{
						if (plr.isOnlineInt() == 1)
						{
							final ClientHardwareInfoHolder hwi = plr.getClient().getHardwareInfo();
							if ((hwi != null) && hwi.equals(hwInfo))
							{
								count++;
							}
						}
					}
					
					if (count > ServerConfig.MAX_PLAYERS_PER_HWID)
					{
						Disconnection.of(client).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
					}
				}
			}, 5000);
		}
		
		// Check if the player is a Dark Elf and has the Shadow Sense skill.
		if ((player.getRace() == Race.DARK_ELF))
		{
			final Skill shadowSense = player.getKnownSkill(CommonSkill.SHADOW_SENSE.getId());
			if (shadowSense != null)
			{
				boolean isNight = GameTimeTaskManager.getInstance().isNight();
				if (isNight)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(shadowSense));
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(shadowSense));
				}
			}
		}
		
		AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OFFLINE_PLAY, player);
		
		// EnterWorld has finished.
		player.setEnteredWorld();
		
		// Wedding checks.
		if (WeddingConfig.ALLOW_WEDDING)
		{
			final int playerObjectId = player.getObjectId();
			for (Couple couple : CoupleManager.getInstance().getCouples())
			{
				if ((couple.getPlayer1Id() == playerObjectId) || (couple.getPlayer2Id() == playerObjectId))
				{
					if (couple.getMaried())
					{
						player.setMarried(true);
					}
					
					player.setCoupleId(couple.getId());
					
					if (couple.getPlayer1Id() == playerObjectId)
					{
						player.setPartnerId(couple.getPlayer2Id());
					}
					else
					{
						player.setPartnerId(couple.getPlayer1Id());
					}
				}
			}
			
			final int partnerId = player.getPartnerId();
			if (partnerId != 0)
			{
				final Player partner = World.getInstance().getPlayer(partnerId);
				if (partner != null)
				{
					partner.sendMessage("Your partner has logged in.");
				}
			}
		}
		
		if ((player.hasPremiumStatus() || !PremiumSystemConfig.PC_CAFE_ONLY_PREMIUM) && PremiumSystemConfig.PC_CAFE_RETAIL_LIKE)
		{
			PcCafePointsManager.getInstance().run(player);
		}
	}
	
	private void notifyClanMembers(Player player)
	{
		final Clan clan = player.getClan();
		if (clan != null)
		{
			clan.getClanMember(player.getObjectId()).setPlayer(player);
			
			final SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME);
			msg.addString(player.getName());
			clan.broadcastToOtherOnlineMembers(msg, player);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
		}
	}
	
	private void loadTutorial(Player player)
	{
		final QuestState qs = player.getQuestState("Q00255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
}
