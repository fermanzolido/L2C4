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
		_info = new ArrayList<>(friendList.size());
		if (friendList.isEmpty())
		{
			return;
		}

		final Map<Integer, FriendInfo> friendInfoMap = new HashMap<>();
		final List<Integer> offlineFriendIds = new ArrayList<>();
		for (int objId : friendList)
		{
			final Player friend = World.getInstance().getPlayer(objId);
			if (friend != null)
			{
				friendInfoMap.put(objId, new FriendInfo(objId, friend.getName(), friend.isOnline(), friend.getPlayerClass().getId(), friend.getLevel()));
			}
			else
			{
				offlineFriendIds.add(objId);
			}
		}

		if (!offlineFriendIds.isEmpty())
		{
			final StringBuilder sb = new StringBuilder("SELECT charId, char_name, online, classid, level FROM characters WHERE charId IN (");
			for (int i = 0; i < offlineFriendIds.size(); i++)
			{
				sb.append("?,");
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");

			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement(sb.toString()))
			{
				for (int i = 0; i < offlineFriendIds.size(); i++)
				{
					statement.setInt(i + 1, offlineFriendIds.get(i));
				}

				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						final int objId = rset.getInt("charId");
						friendInfoMap.put(objId, new FriendInfo(objId, rset.getString("char_name"), rset.getInt("online") == 1, rset.getInt("classid"), rset.getInt("level")));
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
			final FriendInfo info = friendInfoMap.get(objId);
			if (info != null)
			{
				_info.add(info);
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
