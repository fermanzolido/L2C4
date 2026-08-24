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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Support for "Chat with Friends" dialog. <br />
 * This packet is sent only at login.
 * @author mrTJO, UnAfraid
 */
public class FriendListExtended extends ServerPacket
{
	private final List<FriendInfo> _info;
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		int _classid;
		int _level;
		
		public FriendInfo(int objId, String name, boolean online, int classid, int level)
		{
			_objId = objId;
			_name = name;
			_online = online;
			_classid = classid;
			_level = level;
		}
	}
	
	public FriendListExtended(Player player)
	{
		final Collection<Integer> friendList = player.getFriendList();
		if (friendList.isEmpty())
		{
			_info = Collections.emptyList();
			return;
		}

		_info = new ArrayList<>(friendList.size());
		final List<Integer> offlineFriends = new ArrayList<>();
		for (int objId : friendList)
		{
			final Player friend = World.getInstance().getPlayer(objId);
			if ((friend == null) || !friend.isOnline())
			{
				offlineFriends.add(objId);
			}
		}

		final Map<Integer, FriendInfo> offlineInfo = new HashMap<>();
		if (!offlineFriends.isEmpty())
		{
			final StringBuilder sb = new StringBuilder("SELECT charId, char_name, online, classid, level FROM characters WHERE charId IN (");
			for (int i = 0; i < offlineFriends.size(); i++)
			{
				sb.append("?,");
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");

			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement(sb.toString()))
			{
				for (int i = 0; i < offlineFriends.size(); i++)
				{
					ps.setInt(i + 1, offlineFriends.get(i));
				}

				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						final int objId = rs.getInt(1);
						offlineInfo.put(objId, new FriendInfo(objId, rs.getString(2), rs.getInt(3) == 1, rs.getInt(4), rs.getInt(5)));
					}
				}
			}
			catch (Exception e)
			{
				// Who cares?
			}
		}

		for (int objId : friendList)
		{
			final Player onlineFriend = World.getInstance().getPlayer(objId);
			if ((onlineFriend != null) && onlineFriend.isOnline())
			{
				_info.add(new FriendInfo(objId, onlineFriend.getName(), true, onlineFriend.getPlayerClass().getId(), onlineFriend.getLevel()));
			}
			else
			{
				final FriendInfo info = offlineInfo.get(objId);
				if (info != null)
				{
					_info.add(info);
				}
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.FRIEND_LIST.writeId(this, buffer);
		buffer.writeInt(_info.size());
		for (FriendInfo info : _info)
		{
			buffer.writeInt(info._objId); // character id
			buffer.writeString(info._name);
			buffer.writeInt(info._online); // online
			buffer.writeInt(info._online ? info._objId : 0); // object id if online
			buffer.writeInt(info._classid);
			buffer.writeInt(info._level);
		}
	}
}
