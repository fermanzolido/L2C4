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
package ai.others.MonumentOfHeroes;

import org.l2jmobius.gameserver.config.OlympiadConfig;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.model.script.Script;
import org.l2jmobius.gameserver.util.ArrayUtil;

/**
 * Monument of Heroes AI.
 * @author Adry_85, Skache
 */
public class MonumentOfHeroes extends Script
{
	// NPCs
	private static final int[] MONUMENTS =
	{
		31690,
		31769,
		31770,
		31771,
		31772
	};
	
	// Items
	private static final int WINGS_OF_DESTINY_CIRCLET = 6842;
	private static final int[] WEAPONS =
	{
		6611, // Infinity Blade
		6612, // Infinity Cleaver
		6613, // Infinity Axe
		6614, // Infinity Rod
		6615, // Infinity Crusher
		6616, // Infinity Scepter
		6617, // Infinity Stinger
		6618, // Infinity Fang
		6619, // Infinity Bow
		6620, // Infinity Wing
		6621, // Infinity Spear
	};
	
	private MonumentOfHeroes()
	{
		if (OlympiadConfig.OLYMPIAD_ENABLED)
		{
			addStartNpc(MONUMENTS);
			addTalkId(MONUMENTS);
		}
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "HeroClaim":
			{
				if (Hero.getInstance().isHero(player.getObjectId()))
				{
					return "already_hero_status.htm";
				}
				else if (!Hero.getInstance().isUnclaimedHero(player.getObjectId()))
				{
					return "no_hero_status.htm";
				}
				else
				{
					return "receive_hero.htm";
				}
			}
			case "HeroReceive":
			{
				// "HeroClaim" above only decides which page to show. This is the branch that
				// actually grants the status, so it has to repeat the same checks: reaching
				// it does not require having gone through the other one.
				if (Hero.getInstance().isHero(player.getObjectId()))
				{
					return "already_hero_status.htm";
				}
				
				if (!Hero.getInstance().isUnclaimedHero(player.getObjectId()))
				{
					return "no_hero_status.htm";
				}
				
				Hero.getInstance().claimHero(player);
				return null;
			}
			case "HeroWeapon":
			{
				if (player.isHero())
				{
					return hasAtLeastOneQuestItem(player, WEAPONS) ? "already_have_weapon.htm" : "weapon_list.htm";
				}
				
				return "not_a_hero.htm";
			}
			case "HeroCirclet":
			{
				if (player.isHero())
				{
					if (!hasQuestItems(player, WINGS_OF_DESTINY_CIRCLET))
					{
						giveItems(player, WINGS_OF_DESTINY_CIRCLET, 1);
					}
					else
					{
						return "already_have_circlet.htm";
					}
				}
				else
				{
					return "not_a_hero.htm";
				}
				break;
			}
			default:
			{
				// Same reasoning as "HeroReceive": "HeroWeapon" above only picks a page, and
				// this is where the weapon is handed over, so it repeats both of that
				// branch's conditions rather than assuming the player came through it.
				if (!player.isHero())
				{
					return "not_a_hero.htm";
				}
				
				if (hasAtLeastOneQuestItem(player, WEAPONS))
				{
					return "already_have_weapon.htm";
				}
				
				final int weaponId;
				try
				{
					weaponId = Integer.parseInt(event);
				}
				catch (NumberFormatException e)
				{
					// Any event this script does not define lands here.
					break;
				}
				
				if (ArrayUtil.contains(WEAPONS, weaponId))
				{
					giveItems(player, weaponId, 1);
				}
				break;
			}
		}
		
		return super.onEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new MonumentOfHeroes();
	}
}
