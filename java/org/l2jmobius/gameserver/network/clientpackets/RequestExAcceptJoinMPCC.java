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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.groups.CommandChannel;
import org.l2jmobius.gameserver.model.groups.Party;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * format: (ch) d
 * @author -Wooden-
 */
public class RequestExAcceptJoinMPCC extends ClientPacket
{
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player != null)
		{
			final Player requestor = player.getActiveRequester();
			SystemMessage sm;
			if (requestor == null)
			{
				return;
			}
			
			if (_response == 1)
			{
				// Either party can be gone by the time the answer arrives. The packet that
				// sends the invitation tests isInParty() before every getParty(); this one
				// tested neither, and still has to clear the pending request below.
				final Party requestorParty = requestor.getParty();
				final Party playerParty = player.getParty();
				if ((requestorParty == null) || (playerParty == null))
				{
					requestor.sendMessage("The Command Channel invitation could no longer be completed.");
				}
				else
				{
					boolean newCc = false;
					if (!requestorParty.isInCommandChannel())
					{
						new CommandChannel(requestor); // Create new CC
						sm = new SystemMessage(SystemMessageId.A_COMMAND_CHANNEL_HAS_BEEN_OPENED);
						requestor.sendPacket(sm);
						newCc = true;
					}
					
					requestorParty.getCommandChannel().addParty(playerParty);
					if (!newCc)
					{
						sm = new SystemMessage(SystemMessageId.YOU_HAVE_PARTICIPATED_IN_THE_COMMAND_CHANNEL);
						player.sendPacket(sm);
					}
				}
			}
			else
			{
				requestor.sendMessage("The player declined to join your Command Channel.");
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}
