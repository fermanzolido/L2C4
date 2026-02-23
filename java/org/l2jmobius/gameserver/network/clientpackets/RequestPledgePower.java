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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.ClanPrivileges;
import org.l2jmobius.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends ClientPacket
{
	private int _clanMemberId;
	private int _action;
	private int _privs;
	
	@Override
	protected void readImpl()
	{
		_clanMemberId = readInt();
		_action = readInt();
		
		if (_action == 3)
		{
			_privs = readInt();
		}
		else
		{
			_privs = 0;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getClan() != null)
		{
			Player member = null;
			if (player.getClan().getClanMember(_clanMemberId) != null)
			{
				member = player.getClan().getClanMember(_clanMemberId).getPlayer();
			}
			
			switch (_action)
			{
				case 1:
				{
					player.sendPacket(new ManagePledgePower(player.getClanPrivileges().getMask()));
					break;
				}
				
				case 2:
				{
					
					if (member != null)
					{
						player.sendPacket(new ManagePledgePower(member.getClanPrivileges().getMask()));
					}
					break;
				}
				case 3:
				{
					if (player.isClanLeader())
					{
						if (member != null)
						{
							member.setClanPrivileges(new ClanPrivileges(_privs));
						}
					}
					break;
				}
			}
		}
	}
}
