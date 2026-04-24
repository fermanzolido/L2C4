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
package org.l2jmobius.gameserver.model.actor.instance;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.config.custom.SchemeBufferConfig;
import org.l2jmobius.gameserver.data.SchemeBufferTable;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.HtmlUtil;

public class SchemeBuffer extends Npc
{
	private static final int PAGE_LIMIT = 6;
	private static final Pattern SCHEME_NAME_PATTERN = Pattern.compile("^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} .,\\-+!?]+$");
	
	public SchemeBuffer(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String commandValue)
	{
		final StringTokenizer st = new StringTokenizer(commandValue, ";");
		final String currentCommand = st.nextToken();
		if (currentCommand.startsWith("menu"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("cleanup"))
		{
			player.stopAllEffects();
			
			final Summon summon = player.getSummon();
			if (summon != null)
			{
				summon.stopAllEffects();
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("heal"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			
			final Summon summon = player.getSummon();
			if (summon != null)
			{
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("support"))
		{
			showGiveBuffsWindow(player);
		}
		else if (currentCommand.startsWith("givebuffs"))
		{
			final String schemeName = st.nextToken();
			final int cost = Integer.parseInt(st.nextToken());
			Creature target = null;
			if (st.hasMoreTokens())
			{
				final String targetType = st.nextToken();
				if ((targetType != null) && targetType.equalsIgnoreCase("pet"))
				{
					target = player.getSummon();
				}
			}
			else
			{
				target = player;
			}
			
			if (target == null)
			{
				player.sendMessage("You don't have a pet.");
			}
			else if ((cost == 0) || ((SchemeBufferConfig.BUFFER_ITEM_ID == 57) && player.reduceAdena(ItemProcessType.FEE, cost, this, true)) || ((SchemeBufferConfig.BUFFER_ITEM_ID != 57) && player.destroyItemByItemId(ItemProcessType.FEE, SchemeBufferConfig.BUFFER_ITEM_ID, cost, player, true)))
			{
				for (int skillId : SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName))
				{
					SkillData.getInstance().getSkill(skillId, SchemeBufferTable.getInstance().getAvailableBuff(skillId).getLevel()).applyEffects(this, target);
				}
			}
		}
		else if (currentCommand.startsWith("editschemes"))
		{
			showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		}
		else if (currentCommand.startsWith("skill"))
		{
			final String groupType = st.nextToken();
			final String schemeName = st.nextToken();
			final int skillId = Integer.parseInt(st.nextToken());
			final int page = Integer.parseInt(st.nextToken());
			final List<Integer> skills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
			{
				final Skill skill = SkillData.getInstance().getSkill(skillId, 1);
				final int totalBuffs = skills.size();
				final int currentDanceSongCount = getCountOf(skills, true); // true = dances/songs
				final boolean isDanceOrSong = skill.isDance();
				final int maxCount = player.getStat().getMaxBuffCount();
				if (totalBuffs >= maxCount)
				{
					player.sendMessage("This scheme has reached the maximum amount of buffs.");
				}
				else if (isDanceOrSong && (currentDanceSongCount >= PlayerConfig.DANCES_MAX_AMOUNT))
				{
					player.sendMessage("You cannot add more than " + PlayerConfig.DANCES_MAX_AMOUNT + " songs/dances to this scheme.");
				}
				else
				{
					skills.add(skillId);
				}
			}
			else if (currentCommand.startsWith("skillunselect"))
			{
				skills.remove(Integer.valueOf(skillId));
			}
			
			showEditSchemeWindow(player, groupType, schemeName, page);
		}
		else if (currentCommand.startsWith("createscheme"))
		{
			try
			{
				String schemeName = "";
				if (currentCommand.length() > 12)
				{
					schemeName = currentCommand.substring(12).trim();
				}

				if (schemeName.isEmpty() && st.hasMoreTokens())
				{
					schemeName = st.nextToken().trim();
				}

				if (schemeName.isEmpty())
				{
					throw new Exception();
				}

				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must contain up to 14 chars.");
					return;
				}
				
				if (!SCHEME_NAME_PATTERN.matcher(schemeName).matches())
				{
					player.sendMessage("Please use plain alphanumeric characters.");
					return;
				}
				
				final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == SchemeBufferConfig.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}
					
					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}
				
				SchemeBufferTable.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
				showGiveBuffsWindow(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Scheme's name must contain up to 14 chars.");
			}
		}
		else if (currentCommand.startsWith("deletescheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
				final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if ((schemes != null) && schemes.containsKey(schemeName))
				{
					schemes.remove(schemeName);
				}
			}
			catch (Exception e)
			{
				player.sendMessage("This scheme name is invalid.");
			}
			
			showGiveBuffsWindow(player);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int value)
	{
		String filename = "";
		if (value == 0)
		{
			filename = Integer.toString(npcId);
		}
		else
		{
			filename = npcId + "-" + value;
		}
		
		return "data/html/mods/SchemeBuffer/" + filename + ".htm";
	}
	
	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
	 * @param player : The player to make checks on.
	 */
	private void showGiveBuffsWindow(Player player)
	{
		final StringBuilder sb = new StringBuilder(200);
		final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
		if ((schemes == null) || schemes.isEmpty())
		{
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		}
		else
		{
			for (Entry<String, List<Integer>> scheme : schemes.entrySet())
			{
				final int cost = getFee(scheme.getValue());
				sb.append("<font color=\"LEVEL\">" + scheme.getKey() + " [" + scheme.getValue().size() + " skill(s)]" + ((cost > 0) ? " - cost: " + NumberFormat.getInstance(Locale.ENGLISH).format(cost) : "") + "</font><br1>");
				sb.append("<a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + "\">Use on Me</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + ";pet\">Use on Pet</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass npc_%objectId%_editschemes;Buffs;" + scheme.getKey() + ";1\">Edit</a>&nbsp;|&nbsp;");
				sb.append("<a action=\"bypass npc_%objectId%_deletescheme;" + scheme.getKey() + "\">Delete</a><br>");
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player, getHtmlPath(getId(), 1));
		html.replace("%schemes%", sb.toString());
		html.replace("%max_schemes%", SchemeBufferConfig.BUFFER_MAX_SCHEMES);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param page The page.
	 */
	private void showEditSchemeWindow(Player player, String groupType, String schemeName, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		html.setFile(player, getHtmlPath(getId(), 2));
		html.replace("%schemename%", schemeName);
		html.replace("%count%", (getCountOf(schemeSkills, false) + getCountOf(schemeSkills, true)) + " / " + player.getStat().getMaxBuffCount() + " buffs");
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	/**
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param pageValue The page.
	 * @return a String representing skills available to selection for a given groupType.
	 */
	private String getGroupSkillList(Player player, String groupType, String schemeName, int pageValue)
	{
		// Retrieve the entire skills list based on group type.
		List<Integer> skills = SchemeBufferTable.getInstance().getSkillsIdsByType(groupType);
		if (skills.isEmpty())
		{
			return "That group doesn't contain any skills.";
		}
		
		// Calculate page number.
		final int max = HtmlUtil.countPageNumber(skills.size(), PAGE_LIMIT);
		int page = pageValue;
		if (page > max)
		{
			page = max;
		}
		
		// Cut skills list up to page number.
		skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));
		
		final List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		final StringBuilder sb = new StringBuilder(skills.size() * 150);
		int row = 0;
		for (int skillId : skills)
		{
			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));
			
			final Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			if (schemeSkills.contains(skillId))
			{
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
			}
			else
			{
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button action=\"bypass npc_%objectId%_skillselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}
		
		// Build page footer.
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		if (page > 1)
		{
			sb.append("<td align=left width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page - 1) + "\">Previous</a></td>");
		}
		else
		{
			sb.append("<td align=left width=70>Previous</td>");
		}
		
		sb.append("<td align=center width=100>Page " + page + "</td>");
		if (page < max)
		{
			sb.append("<td align=right width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page + 1) + "\">Next</a></td>");
		}
		else
		{
			sb.append("<td align=right width=70>Next</td>");
		}
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		return sb.toString();
	}
	
	/**
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @return a string representing all groupTypes available. The group currently on selection isn't linkable.
	 */
	private static String getTypesFrame(String groupType, String schemeName)
	{
		final StringBuilder sb = new StringBuilder(500);
		sb.append("<table>");
		
		int count = 0;
		for (String type : SchemeBufferTable.getInstance().getSkillTypes())
		{
			if (count == 0)
			{
				sb.append("<tr>");
			}
			
			if (groupType.equalsIgnoreCase(type))
			{
				sb.append("<td width=65>" + type + "</td>");
			}
			else
			{
				sb.append("<td width=65><a action=\"bypass npc_%objectId%_editschemes;" + type + ";" + schemeName + ";1\">" + type + "</a></td>");
			}
			
			count++;
			if (count == 4)
			{
				sb.append("</tr>");
				count = 0;
			}
		}
		
		if (!sb.toString().endsWith("</tr>"))
		{
			sb.append("</tr>");
		}
		
		sb.append("</table>");
		
		return sb.toString();
	}
	
	/**
	 * @param list : A list of skill ids.
	 * @return a global fee for all skills contained in list.
	 */
	private static int getFee(List<Integer> list)
	{
		if (SchemeBufferConfig.BUFFER_STATIC_BUFF_COST > 0)
		{
			return list.size() * SchemeBufferConfig.BUFFER_STATIC_BUFF_COST;
		}
		
		int fee = 0;
		for (int sk : list)
		{
			fee += SchemeBufferTable.getInstance().getAvailableBuff(sk).getPrice();
		}
		
		return fee;
	}
	
	private static int getCountOf(List<Integer> skills, boolean dances)
	{
		int count = 0;
		for (int skillId : skills)
		{
			if (SkillData.getInstance().getSkill(skillId, 1).isDance() == dances)
			{
				count++;
			}
		}
		
		return count;
	}
}
