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
package handlers.itemhandlers;

import java.util.logging.Level;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.enums.ShotType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.holders.SkillHolder;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Beast SpiritShot Handler
 * @author Tempy
 */
public class BeastSpiritShot implements IItemHandler
{
	@Override
	public boolean onItemUse(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE);
			return false;
		}
		
		final Player activeOwner = playable.asPlayer();
		if (!activeOwner.hasSummon())
		{
			activeOwner.sendPacket(SystemMessageId.NO_SERVITORS_OR_PETS_AVAILABLE);
			return false;
		}
		
		if (activeOwner.getSummon().isDead())
		{
			activeOwner.sendPacket(SystemMessageId.WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE);
			return false;
		}
		
		final int itemId = item.getId();
		final boolean isBlessed = ((itemId == 6647) || (itemId == 20334)); // Spirit shot IDs are hardcoded per pet type.
		final short shotConsumption = activeOwner.getSummon().getSpiritShotsPerHit();
		final SkillHolder[] skills = item.getTemplate().getSkills();
		if (skills == null)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		
		final long shotCount = item.getCount();
		if (shotCount < shotConsumption)
		{
			// Not enough SpiritShots to use.
			if (!activeOwner.disableAutoShot(itemId))
			{
				activeOwner.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
			}
			
			return false;
		}
		
		if (activeOwner.getSummon().isChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS))
		{
			// shots are already active.
			return false;
		}
		
		if (!activeOwner.destroyItem(ItemProcessType.NONE, item.getObjectId(), shotConsumption, null, false))
		{
			if (!activeOwner.disableAutoShot(itemId))
			{
				activeOwner.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
			}
			
			return false;
		}
		
		// Pet uses the power of spirit.
		activeOwner.sendPacket(SystemMessageId.PET_USES_THE_POWER_OF_SPIRIT);
		activeOwner.getSummon().setChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, true);
		activeOwner.broadcastSkillPacket(new MagicSkillUse(activeOwner.getSummon(), activeOwner.getSummon(), skills[0].getSkillId(), skills[0].getSkillLevel(), 0, 0), activeOwner);
		return true;
	}
}
