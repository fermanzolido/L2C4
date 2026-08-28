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


import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.commons.util.TraceUtil;
import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.handler.AdminCommandHandler;
import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.managers.CaptchaManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.holders.actor.npc.OnNpcManorBypass;
import org.l2jmobius.gameserver.model.events.holders.actor.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.LocationUtil;

/**
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public class RequestBypassToServer extends ClientPacket
{
	private static final String[] _possibleNonHtmlCommands =
	{
		"_bbs",
		"bbs",
		"_mail",
		"_friend",
		"_match",
		"_diary",
		"OlympiadArenaChange",
		"manor_menu_select",
		"report"
	};
	
	// S
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readString();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_command.isEmpty())
		{
			PacketLogger.warning(player + " sent empty bypass!");
			Disconnection.of(getClient(), player).storeAndDeleteWith(LeaveWorld.STATIC_PACKET);
			return;
		}
		
		boolean requiresBypassValidation = true;
		for (String possibleNonHtmlCommand : _possibleNonHtmlCommands)
		{
			if (_command.startsWith(possibleNonHtmlCommand))
			{
				requiresBypassValidation = false;
				break;
			}
		}
		
		int bypassOriginId = 0;
		if (requiresBypassValidation)
		{
			bypassOriginId = player.validateHtmlAction(_command);
			if (bypassOriginId == -1)
			{
				return;
			}
			
			if ((bypassOriginId > 0) && !LocationUtil.isInsideRangeOfObjectId(player, bypassOriginId, Npc.INTERACTION_DISTANCE))
			{
				// No logging here, this could be a common case where the player has the html still open and run too far away and then clicks a html action
				return;
			}
		}
		
		if (!getClient().getFloodProtectors().canUseServerBypass())
		{
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				AdminCommandHandler.getInstance().onCommand(player, _command, true);
			}
			else if (CommunityBoardHandler.getInstance().isCommunityBoardCommand(_command))
			{
				CommunityBoardHandler.getInstance().handleParseCommand(_command, player);
			}
			else if (_command.equals("come_here") && player.isGM())
			{
				comeHere(player);
			}
			else if (_command.startsWith("npc_"))
			{
				final int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				
				final int objectId = bypassInt(id);
				if (objectId >= 0)
				{
					final WorldObject object = World.getInstance().findObject(objectId);
					if ((object != null) && object.isNpc() && (endOfId > 0) && player.isInsideRadius2D(object, Npc.INTERACTION_DISTANCE))
					{
						object.asNpc().onBypassFeedback(player, _command.substring(endOfId + 1));
					}
				}
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("item_"))
			{
				final int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(5, endOfId);
				}
				else
				{
					id = _command.substring(5);
				}
				
				final int objectId = bypassInt(id);
				if (objectId >= 0)
				{
					final Item item = player.getInventory().getItemByObjectId(objectId);
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(player, _command.substring(endOfId + 1));
					}
				}
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("_match"))
			{
				final String params = _command.substring(_command.indexOf('?') + 1);
				final int heroclass = bypassInt(bypassParameter(params, 0));
				final int heropage = bypassInt(bypassParameter(params, 1));
				if ((heroclass >= 0) && (heropage >= 0))
				{
					final int heroid = Hero.getInstance().getHeroByClass(heroclass);
					if (heroid > 0)
					{
						Hero.getInstance().showHeroFights(player, heroclass, heroid, heropage);
					}
				}
			}
			else if (_command.startsWith("_diary"))
			{
				final String params = _command.substring(_command.indexOf('?') + 1);
				final int heroclass = bypassInt(bypassParameter(params, 0));
				final int heropage = bypassInt(bypassParameter(params, 1));
				if ((heroclass >= 0) && (heropage >= 0))
				{
					final int heroid = Hero.getInstance().getHeroByClass(heroclass);
					if (heroid > 0)
					{
						Hero.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
					}
				}
			}
			else if (_command.startsWith("OlympiadArenaChange"))
			{
				Olympiad.bypassChangeArena(_command, player);
			}
			else if (_command.startsWith("manor_menu_select"))
			{
				final Npc lastNpc = player.getLastFolkNPC();
				if (GeneralConfig.ALLOW_MANOR && (lastNpc != null) && lastNpc.canInteract(player) && EventDispatcher.getInstance().hasListener(EventType.ON_NPC_MANOR_BYPASS, lastNpc))
				{
					final String params = _command.substring(_command.indexOf('?') + 1);
					final int ask = bypassInt(bypassParameter(params, 0));
					final int state = bypassInt(bypassParameter(params, 1));
					final String timeParam = bypassParameter(params, 2);
					if ((ask >= 0) && (state >= 0) && (timeParam != null))
					{
						EventDispatcher.getInstance().notifyEventAsync(new OnNpcManorBypass(player, lastNpc, ask, state, timeParam.equals("1")), lastNpc);
					}
				}
			}
			else if (_command.startsWith("report"))
			{
				CaptchaManager.getInstance().analyseBypass(_command, player);
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					if (bypassOriginId > 0)
					{
						final WorldObject bypassOrigin = World.getInstance().findObject(bypassOriginId);
						if ((bypassOrigin != null) && bypassOrigin.isCreature())
						{
							handler.onCommand(_command, player, bypassOrigin.asCreature());
						}
						else
						{
							handler.onCommand(_command, player, null);
						}
					}
					else
					{
						handler.onCommand(_command, player, null);
					}
				}
				else
				{
					PacketLogger.warning(getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			PacketLogger.warning("Exception processing bypass from " + player + ": " + _command + " " + e.getMessage());
			PacketLogger.warning(TraceUtil.getStackTrace(e));
			if (player.isGM())
			{
				final StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste + "<br1>");
				}
				sb.append("</body></html>");
				
				// item html
				final NpcHtmlMessage msg = new NpcHtmlMessage(0, 1, sb.toString());
				msg.disableValidation();
				player.sendPacket(msg);
			}
		}
		
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_BYPASS, player))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerBypass(player, _command), player);
		}
	}
	
	/**
	 * Reads one positional key=value pair out of a bypass query. Three of the branches
	 * above are in the list that skips html action validation, so the query is whatever
	 * the client sent: a missing pair or a missing '=' used to throw out of the method.
	 * @param params the part of the bypass command after the '?'
	 * @param index which pair to read
	 * @return the value, or {@code null} when the query has none there
	 */
	private static String bypassParameter(String params, int index)
	{
		final String[] pairs = params.split("&");
		if (index >= pairs.length)
		{
			return null;
		}
		
		final String[] pair = pairs[index].split("=");
		return pair.length < 2 ? null : pair[1];
	}
	
	/**
	 * @param value the text to read a number out of
	 * @return the number, or -1 when the text is not one that fits in an int. isNumeric
	 *         alone was not enough: it accepts a run of digits of any length, and
	 *         Integer.parseInt does not take all of them.
	 */
	private static int bypassInt(String value)
	{
		if (!StringUtil.isNumeric(value) || (value.length() > 10))
		{
			return -1;
		}
		
		final long parsed = Long.parseLong(value);
		return parsed > Integer.MAX_VALUE ? -1 : (int) parsed;
	}
	
	/**
	 * @param player
	 */
	private void comeHere(Player player)
	{
		final WorldObject obj = player.getTarget();
		if (obj == null)
		{
			return;
		}
		
		if (obj instanceof Npc)
		{
			final Npc temp = obj.asNpc();
			temp.setTarget(player);
			temp.getAI().setIntention(Intention.MOVE_TO, player.getLocation());
		}
	}
}
