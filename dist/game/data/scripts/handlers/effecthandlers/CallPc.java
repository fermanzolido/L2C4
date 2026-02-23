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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.data.xml.ItemData;
import org.l2jmobius.gameserver.managers.InstanceManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.holders.player.SummonRequestHolder;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Call Pc effect implementation.
 * @author Adry_85
 */
public class CallPc extends AbstractEffect
{
	private final int _itemId;
	private final int _itemCount;
	
	public CallPc(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_itemId = params.getInt("itemId", 0);
		_itemCount = params.getInt("itemCount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (effected == effector)
		{
			return;
		}
		
		final Player target = effected.asPlayer();
		final Player player = effector.asPlayer();
		if (player != null)
		{
			if (checkSummonTargetStatus(target, player))
			{
				if ((_itemId != 0) && (_itemCount != 0))
				{
					if (target.getInventory().getInventoryItemCount(_itemId, 0) < _itemCount)
					{
						final ItemTemplate template = ItemData.getInstance().getTemplate(_itemId);
						if (template != null)
						{
							target.sendMessage(template.getName() + " is required for summoning.");
						}
						return;
					}
					
					target.getInventory().destroyItemByItemId(null, _itemId, _itemCount, player, target);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
					sm.addItemName(_itemId);
					target.sendPacket(sm);
				}
				
				target.addScript(new SummonRequestHolder(player));
				
				// final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
				// confirm.getSystemMessage().addString(player.getName());
				// confirm.getSystemMessage().addZoneName(player.getX(), player.getY(), player.getZ());
				// confirm.addTime(30000);
				// confirm.addRequesterId(player.getObjectId());
				// target.sendPacket(confirm);
			}
		}
		else if (target != null)
		{
			final WorldObject previousTarget = target.getTarget();
			target.teleToLocation(effector);
			target.setTarget(previousTarget);
		}
	}
	
	public static boolean checkSummonTargetStatus(Player target, Player activeChar)
	{
		if (target == activeChar)
		{
			return false;
		}
		
		if (target.isAlikeDead())
		{
			activeChar.sendMessage(target.getName() + " is dead at the moment and cannot be summoned.");
			return false;
		}
		
		if (target.isInStoreMode())
		{
			activeChar.sendMessage(target.getName() + " is currently trading or operating a private store and cannot be summoned.");
			return false;
		}
		
		if (target.isRooted() || target.isInCombat())
		{
			activeChar.sendMessage(target.getName() + " is engaged in combat and cannot be summoned.");
			return false;
		}
		
		if (target.isInOlympiadMode() || Olympiad.getInstance().isRegisteredInComp(target))
		{
			activeChar.sendMessage("You cannot summon players who are currently participating in the Grand Olympiad.");
			return false;
		}
		
		if (target.isFestivalParticipant() || target.isOnEvent())
		{
			activeChar.sendMessage("Your target is in an area which blocks summoning.");
			return false;
		}
		
		if (target.inObserverMode())
		{
			activeChar.sendMessage(target.getName() + " is in a state which prevents summoning.");
			return false;
		}
		
		if (target.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || target.isInsideZone(ZoneId.JAIL))
		{
			activeChar.sendMessage(target.getName() + " is in a state which prevents summoning.");
			return false;
		}
		
		if (activeChar.getInstanceId() > 0)
		{
			final Instance summonerInstance = InstanceManager.getInstance().getInstance(activeChar.getInstanceId());
			if (!GeneralConfig.ALLOW_SUMMON_IN_INSTANCE || !summonerInstance.isSummonAllowed())
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}
		
		// Seven Signs dungeon summon restriction: summoning is blocked due to mob teleport behavior not being implemented.
		if (activeChar.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(target.getObjectId());
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					activeChar.sendMessage("Your target is in an area which blocks summoning.");
					return false;
				}
			}
			else if (targetCabal == SevenSigns.CABAL_NULL)
			{
				activeChar.sendMessage("Your target is in an area which blocks summoning.");
				return false;
			}
		}
		
		return true;
	}
}
