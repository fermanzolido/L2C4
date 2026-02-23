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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.model.Crest;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanAccess;
import org.l2jmobius.gameserver.model.clan.enums.CrestType;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Format : chdb c (id) 0xD0 h (subid) 0x11 d data size b raw data (picture i think ;) )
 * @author -Wooden-
 */
public class RequestExSetPledgeCrestLarge extends ClientPacket
{
	private int _length;
	private byte[] _data = null;
	
	@Override
	protected void readImpl()
	{
		_length = readInt();
		if (_length > 2176)
		{
			return;
		}
		
		_data = readBytes(_length);
	}
	
	@Override
	protected void runImpl()
	{
		if (_data == null)
		{
			return;
		}
		
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		
		if ((_length < 0) || (_length > 2176))
		{
			player.sendMessage("The size of the uploaded crest or insignia does not meet the standard requirements.");
			return;
		}
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_REGISTRATION_OR_DELETION_OF_A_CLAN_S_CREST_IS_NOT_ALLOWED);
			return;
		}
		
		if (!player.hasAccess(ClanAccess.CHANGE_CREST))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (_length == 0)
		{
			if (clan.getCrestLargeId() != 0)
			{
				clan.changeLargeCrest(0);
				player.sendMessage("The clan's crest has been deleted.");
			}
		}
		else
		{
			if (clan.getLevel() < 3)
			{
				player.sendPacket(SystemMessageId.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLAN_S_SKILL_LEVELS_ARE_ABOVE_3);
				return;
			}
			
			final Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.PLEDGE_LARGE);
			if (crest != null)
			{
				clan.changeLargeCrest(crest.getId());
				player.sendPacket(SystemMessageId.THE_CLAN_S_EMBLEM_WAS_SUCCESSFULLY_REGISTERED_ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
			}
		}
	}
	
}
