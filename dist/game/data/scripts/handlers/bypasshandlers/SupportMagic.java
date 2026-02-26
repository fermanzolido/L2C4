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
package handlers.bypasshandlers;

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author Mobius
 */
public class SupportMagic implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"supportmagic"
	};
	
	// Levels
	private static final int LOWEST_LEVEL = 1;
	private static final int HIGHEST_LEVEL = 40;
	
	@Override
	public boolean onCommand(String command, Player player, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken(); // Skip command
		int stage = 0;
		if (st.hasMoreTokens())
		{
			try
			{
				stage = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException e)
			{
				// ignore
			}
		}

		final int level = player.getLevel();
		final Npc npc = target.asNpc();
		if (!PlayerConfig.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE && !player.isNewbie())
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicNovice.htm");
			return false;
		}
		else if (level > HIGHEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicHighLevel.htm");
			return false;
		}
		else if (level < LOWEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicLowLevel.htm");
			return false;
		}
		else if (player.getPlayerClass().level() == 3)
		{
			player.sendMessage("Only adventurers who have not completed their 3rd class transfer may receive these buffs.");
			return false;
		}

		if (stage == 0)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagic.htm");
			return true;
		}

		npc.setTarget(player);

		if ((stage == 1) && (level <= 20))
		{
			giveStage1Buffs(player, npc);
		}
		else if ((stage == 2) && (level >= 20) && (level <= 40))
		{
			giveStage2Buffs(player, npc);
		}
		else
		{
			if ((stage == 1) && (level > 20))
			{
				player.sendMessage("You are too experienced for the beginner buffs.");
			}
			if ((stage == 2) && (level < 20))
			{
				player.sendMessage("You are not experienced enough for the pro buffs.");
			}
			return false;
		}
		
		return true;
	}
	
	private void giveStage1Buffs(Player player, Npc npc)
	{
		SkillData.getInstance().getSkill(1204, 2).applyEffects(npc, player); // Wind Walk
		SkillData.getInstance().getSkill(1040, 3).applyEffects(npc, player); // Shield
		SkillData.getInstance().getSkill(4338, 1).applyEffects(npc, player); // Life Cubic

		if (player.isMageClass())
		{
			SkillData.getInstance().getSkill(1085, 3).applyEffects(npc, player); // Acumen
		}
		else
		{
			SkillData.getInstance().getSkill(1068, 3).applyEffects(npc, player); // Might
		}
	}

	private void giveStage2Buffs(Player player, Npc npc)
	{
		giveStage1Buffs(player, npc);

		SkillData.getInstance().getSkill(1045, 2).applyEffects(npc, player); // Bless the Body

		// Pro Buffs
		SkillData.getInstance().getSkill(4367, 1).applyEffects(npc, player); // Clan Hall Manager Buff (MP Regen)
		SkillData.getInstance().getSkill(1259, 2).applyEffects(npc, player); // Resist Shock

		if (player.isMageClass())
		{
			SkillData.getInstance().getSkill(1059, 2).applyEffects(npc, player); // Empower
			SkillData.getInstance().getSkill(1078, 2).applyEffects(npc, player); // Concentration
		}
		else
		{
			SkillData.getInstance().getSkill(1086, 2).applyEffects(npc, player); // Haste
			SkillData.getInstance().getSkill(1268, 2).applyEffects(npc, player); // Vampiric Rage
			SkillData.getInstance().getSkill(1077, 2).applyEffects(npc, player); // Focus
		}
	}

	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}
}
