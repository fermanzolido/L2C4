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
package org.l2jmobius.gameserver.config.custom;

import java.util.Arrays;
import java.util.logging.Logger;

import org.l2jmobius.commons.util.ConfigReader;
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;

/**
 * This class loads all the custom class balance related configurations.
 * @author Mobius
 */
public class ClassBalanceConfig
{
	private static final Logger LOGGER = Logger.getLogger(ClassBalanceConfig.class.getName());
	
	// File
	private static final String CLASS_BALANCE_CONFIG_FILE = "./config/Custom/ClassBalance.ini";
	
	// Constants
	public static float[] PVE_MAGICAL_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_MAGICAL_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_MAGICAL_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVP_MAGICAL_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVE_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVP_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVE_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS = new float[119];
	public static float[] PVE_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_BLOW_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_BLOW_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_BLOW_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVP_BLOW_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVE_ENERGY_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVP_ENERGY_SKILL_DAMAGE_MULTIPLIERS = new float[119];
	public static float[] PVE_ENERGY_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PVP_ENERGY_SKILL_DEFENCE_MULTIPLIERS = new float[119];
	public static float[] PLAYER_HEALING_SKILL_MULTIPLIERS = new float[119];
	public static float[] SKILL_MASTERY_CHANCE_MULTIPLIERS = new float[119];
	public static float[] SKILL_REUSE_MULTIPLIERS = new float[119];
	public static float[] EXP_AMOUNT_MULTIPLIERS = new float[119];
	public static float[] SP_AMOUNT_MULTIPLIERS = new float[119];
	
	public static void load()
	{
		final ConfigReader config = new ConfigReader(CLASS_BALANCE_CONFIG_FILE);
		
		loadMultipliers(config, "PveMagicalSkillDamageMultipliers", PVE_MAGICAL_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpMagicalSkillDamageMultipliers", PVP_MAGICAL_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PveMagicalSkillDefenceMultipliers", PVE_MAGICAL_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvpMagicalSkillDefenceMultipliers", PVP_MAGICAL_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PveMagicalSkillCriticalChanceMultipliers", PVE_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PvpMagicalSkillCriticalChanceMultipliers", PVP_MAGICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PveMagicalSkillCriticalDamageMultipliers", PVE_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpMagicalSkillCriticalDamageMultipliers", PVP_MAGICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalSkillDamageMultipliers", PVE_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalSkillDamageMultipliers", PVP_PHYSICAL_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalSkillDefenceMultipliers", PVE_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalSkillDefenceMultipliers", PVP_PHYSICAL_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalSkillCriticalChanceMultipliers", PVE_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalSkillCriticalChanceMultipliers", PVP_PHYSICAL_SKILL_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalSkillCriticalDamageMultipliers", PVE_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalSkillCriticalDamageMultipliers", PVP_PHYSICAL_SKILL_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalAttackDamageMultipliers", PVE_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalAttackDamageMultipliers", PVP_PHYSICAL_ATTACK_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalAttackDefenceMultipliers", PVE_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalAttackDefenceMultipliers", PVP_PHYSICAL_ATTACK_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalAttackCriticalChanceMultipliers", PVE_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalAttackCriticalChanceMultipliers", PVP_PHYSICAL_ATTACK_CRITICAL_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "PvePhysicalAttackCriticalDamageMultipliers", PVE_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpPhysicalAttackCriticalDamageMultipliers", PVP_PHYSICAL_ATTACK_CRITICAL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PveBlowSkillDamageMultipliers", PVE_BLOW_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpBlowSkillDamageMultipliers", PVP_BLOW_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PveBlowSkillDefenceMultipliers", PVE_BLOW_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvpBlowSkillDefenceMultipliers", PVP_BLOW_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PveEnergySkillDamageMultipliers", PVE_ENERGY_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PvpEnergySkillDamageMultipliers", PVP_ENERGY_SKILL_DAMAGE_MULTIPLIERS);
		loadMultipliers(config, "PveEnergySkillDefenceMultipliers", PVE_ENERGY_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PvpEnergySkillDefenceMultipliers", PVP_ENERGY_SKILL_DEFENCE_MULTIPLIERS);
		loadMultipliers(config, "PlayerHealingSkillMultipliers", PLAYER_HEALING_SKILL_MULTIPLIERS);
		loadMultipliers(config, "SkillMasteryChanceMultipliers", SKILL_MASTERY_CHANCE_MULTIPLIERS);
		loadMultipliers(config, "SkillReuseMultipliers", SKILL_REUSE_MULTIPLIERS);
		loadMultipliers(config, "ExpAmountMultipliers", EXP_AMOUNT_MULTIPLIERS);
		loadMultipliers(config, "SpAmountMultipliers", SP_AMOUNT_MULTIPLIERS);
	}
	
	/**
	 * Fills the given table with a neutral 1f and then applies the multipliers named by the given
	 * property, whose format is {@code class*multiplier;class*multiplier}, where {@code class} is
	 * either a numeric {@link PlayerClass} id or a {@link PlayerClass} name.
	 * <p>
	 * Every entry is validated on its own and a bad one is logged, naming the property and the
	 * text that failed, and then skipped. This used to be thirty-seven copies of the same block,
	 * none of which validated anything: an unknown class name threw from Enum.valueOf, an id past
	 * the end of the table threw from the array store, and a multiplier that was not a number threw
	 * from parseFloat. Nothing between here and the GameServer constructor catches any of them, so
	 * a single typo in this optional file stopped the server from starting, with a message that did
	 * not say which property caused it, and left the roughly thirty configuration classes that load
	 * after this one untouched.
	 * @param config the reader for the class balance file
	 * @param property the property holding the multipliers
	 * @param multipliers the table to fill, indexed by class id
	 */
	private static void loadMultipliers(ConfigReader config, String property, float[] multipliers)
	{
		Arrays.fill(multipliers, 1f);
		
		final String value = config.getString(property, "").trim();
		if (value.isEmpty())
		{
			return;
		}
		
		for (String info : value.split(";"))
		{
			final String entry = info.trim();
			if (entry.isEmpty())
			{
				continue;
			}
			
			final String[] classInfo = entry.split("[*]");
			if (classInfo.length != 2)
			{
				LOGGER.warning("ClassBalanceConfig: " + property + ": expected class*multiplier but found \"" + entry + "\".");
				continue;
			}
			
			final String id = classInfo[0].trim();
			final int classId;
			if (StringUtil.isNumeric(id))
			{
				// Through parseInt rather than Integer.parseInt: isNumeric only proves every character
				// is a digit, not that the number fits in an int, and -1 fails the range check below.
				classId = StringUtil.parseInt(id, -1);
			}
			else if (StringUtil.isEnum(id, PlayerClass.class))
			{
				classId = Enum.valueOf(PlayerClass.class, id).getId();
			}
			else
			{
				LOGGER.warning("ClassBalanceConfig: " + property + ": \"" + id + "\" is neither a class id nor a class name.");
				continue;
			}
			
			if ((classId < 0) || (classId >= multipliers.length))
			{
				LOGGER.warning("ClassBalanceConfig: " + property + ": class id " + classId + " is outside 0.." + (multipliers.length - 1) + ".");
				continue;
			}
			
			final String multiplier = classInfo[1].trim();
			if (!StringUtil.isFloat(multiplier))
			{
				LOGGER.warning("ClassBalanceConfig: " + property + ": \"" + multiplier + "\" is not a number.");
				continue;
			}
			
			multipliers[classId] = Float.parseFloat(multiplier);
		}
	}
}
