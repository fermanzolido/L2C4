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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author -Wooden-
 */
public class PledgeReceiveMemberInfo extends ServerPacket
{
	private final Clan _clan;
	private final Player _player;
	
	public PledgeReceiveMemberInfo(ClanMember member, Player player)
	{
		_clan = member.getClan();
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PLEDGE_RECEIVE_MEMBER_INFO.writeId(this, buffer);
		buffer.writeInt(_clan.getId());
		buffer.writeString(_clan.getName());
		buffer.writeString(_clan.getLeaderName());
		buffer.writeInt(_clan.getCrestId()); // crest id .. is used again
		buffer.writeInt(_clan.getLevel());
		buffer.writeInt(_clan.getCastleId());
		buffer.writeInt(_clan.getHideoutId());
		buffer.writeInt(0);
		buffer.writeInt(_player.getLevel()); // ??
		buffer.writeInt(_clan.getDissolvingExpiryTime() > System.currentTimeMillis() ? 3 : 0);
		buffer.writeInt(0);
		buffer.writeInt(_clan.getAllyId());
		buffer.writeString(_clan.getAllyName());
		buffer.writeInt(_clan.getAllyCrestId());
		buffer.writeInt(_clan.isAtWar()); // new c3
		
		final Collection<ClanMember> members = _clan.getMembers();
		buffer.writeInt(members.size() - 1);
		for (ClanMember member : members)
		{
			// Packet structure matches C4.
			if (member.getObjectId() == _player.getObjectId())
			{
				continue;
			}
			
			buffer.writeString(member.getName());
			buffer.writeInt(member.getLevel());
			buffer.writeInt(member.getClassId());
			buffer.writeInt(0);
			buffer.writeInt(1);
			buffer.writeInt(member.isOnline() ? member.getObjectId() : 0); // 1=online 0=offline
		}
	}
}
