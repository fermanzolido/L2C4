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
package handlers.effecthandlers;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Chest;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * Open Chest effect implementation.
 * @author Adry_85, Naker
 */
public class OpenChest extends AbstractEffect
{
	public OpenChest(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill)
	{
		if (!(effected instanceof Chest))
		{
			return;
		}
		
		final Player player = effector.asPlayer();
		final Chest chest = (Chest) effected;
		if (chest.isDead() || (player.getInstanceId() != chest.getInstanceId()))
		{
			return;
		}
		
		final boolean levelCheck = (((player.getLevel() <= 77) && (Math.abs(chest.getLevel() - player.getLevel()) <= 6)) || ((player.getLevel() >= 78) && (Math.abs(chest.getLevel() - player.getLevel()) <= 5)));
		if (!levelCheck)
		{
			player.broadcastSocialAction(13);
			chest.addDamageHate(player, 0, 1);
			chest.getAI().setIntention(Intention.ATTACK, player);
			return;
		}
		
		final int chestLevel = chest.getLevel();
		final int keyLevel = skill.getLevel();
		final int optimalMax = (keyLevel * 10) + 9;
		int successChance = (skill.getId() == 2065) ? 60 : 100;
		if (chestLevel > optimalMax)
		{
			int levelDiff = chestLevel - optimalMax;
			successChance -= (levelDiff * 10);
		}
		
		if ((skill.getId() == 27) || (skill.getId() == 2322) || (skill.getId() == 2400))
		{
			successChance = 100;
		}
		
		if (skill.getId() == 3155)
		{
			successChance = 90;
		}
		
		successChance = Math.max(10, successChance);
		
		if (Rnd.get(100) < successChance)
		{
			player.broadcastSocialAction(3);
			chest.setSpecialDrop();
			chest.setMustRewardExpSp(false);
			chest.reduceCurrentHp(chest.getMaxHp(), player, skill);
		}
		else
		{
			player.broadcastSocialAction(13);
			chest.addDamageHate(player, 0, 1);
			chest.getAI().setIntention(Intention.ATTACK, player);
		}
	}
}
