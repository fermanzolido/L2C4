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

import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.managers.PetitionManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * <p>
 * Format: (c) Sd
 * <ul>
 * <li>S: content</li>
 * <li>d: type</li>
 * </ul>
 * </p>
 * @author -Wooden-, TempyIncursion
 */
public class RequestPetition extends ClientPacket
{
	private String _content;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_content = readString();
		_type = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((_type <= 0) || (_type >= 10))
		{
			return;
		}
		
		if (!AdminData.getInstance().isGmOnline(false))
		{
			player.sendPacket(SystemMessageId.THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY);
			return;
		}
		
		if (!PetitionManager.getInstance().isPetitioningAllowed())
		{
			player.sendPacket(SystemMessageId.CANNOT_CONNECT_TO_PETITION_SERVER);
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerPetitionPending(player))
		{
			player.sendPacket(SystemMessageId.ALREADY_APPLIED_FOR_PETITION);
			return;
		}
		
		if (PetitionManager.getInstance().getPendingPetitionCount() == PlayerConfig.MAX_PETITIONS_PENDING)
		{
			player.sendPacket(SystemMessageId.THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER);
			return;
		}
		
		final int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(player) + 1;
		if (totalPetitions > PlayerConfig.MAX_PETITIONS_PER_PLAYER)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS);
			sm.addInt(totalPetitions);
			player.sendPacket(sm);
			return;
		}
		
		if (_content.length() > 255)
		{
			player.sendPacket(SystemMessageId.PETITIONS_CANNOT_EXCEED_255_CHARACTERS);
			return;
		}
		
		final int petitionId = PetitionManager.getInstance().submitPetition(player, _content, _type);
		SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_APPLICATION_ACCEPTED_RECEIPT_NO_IS_S1);
		sm.addInt(petitionId);
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUBMITTED_S1_PETITIONS_YOU_MAY_SUBMIT_S2_MORE_PETITIONS_TODAY);
		sm.addInt(totalPetitions);
		sm.addInt(PlayerConfig.MAX_PETITIONS_PER_PLAYER - totalPetitions);
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.THERE_ARE_S1_PETITIONS_PENDING);
		sm.addInt(PetitionManager.getInstance().getPendingPetitionCount());
		player.sendPacket(sm);
	}
}
