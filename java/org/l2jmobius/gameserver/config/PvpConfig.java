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
package org.l2jmobius.gameserver.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.commons.util.ConfigReader;
import org.l2jmobius.commons.util.StringUtil;

/**
 * This class loads all the PVP related configurations.
 * @author Mobius
 */
public class PvpConfig
{
	private static final Logger LOGGER = Logger.getLogger(PvpConfig.class.getName());
	
	// File
	private static final String PVP_CONFIG_FILE = "./config/PVP.ini";
	
	// Constants
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static boolean ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	public static void load()
	{
		final ConfigReader config = new ConfigReader(PVP_CONFIG_FILE);
		KARMA_DROP_GM = config.getBoolean("CanGMDropEquipment", false);
		KARMA_AWARD_PK_KILL = config.getBoolean("AwardPKKillPVPPoint", false);
		KARMA_PK_LIMIT = config.getInt("MinimumPKRequiredToDrop", 5);
		KARMA_NONDROPPABLE_PET_ITEMS = config.getString("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
		KARMA_NONDROPPABLE_ITEMS = config.getString("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694");
		KARMA_LIST_NONDROPPABLE_PET_ITEMS = parseItemIdList(KARMA_NONDROPPABLE_PET_ITEMS, "ListOfPetItems");
		KARMA_LIST_NONDROPPABLE_ITEMS = parseItemIdList(KARMA_NONDROPPABLE_ITEMS, "ListOfNonDroppableItems");
		ANTIFEED_ENABLE = config.getBoolean("AntiFeedEnable", false);
		ANTIFEED_DUALBOX = config.getBoolean("AntiFeedDualbox", true);
		ANTIFEED_DISCONNECTED_AS_DUALBOX = config.getBoolean("AntiFeedDisconnectedAsDualbox", true);
		ANTIFEED_INTERVAL = config.getInt("AntiFeedInterval", 120) * 1000;
		PVP_NORMAL_TIME = config.getInt("PvPVsNormalTime", 120000);
		PVP_PVP_TIME = config.getInt("PvPVsPvPTime", 60000);
	}
	
	/**
	 * @param value the comma separated property text
	 * @param property the property name, to name in the warning
	 * @return the item ids the text holds, sorted for the binary searches that read
	 *         them. An entry that is not a number used to throw out of the config load
	 *         and take the whole startup with it, naming neither the property nor the
	 *         entry.
	 */
	private static int[] parseItemIdList(String value, String property)
	{
		final List<Integer> ids = new ArrayList<>();
		for (String entry : value.split(","))
		{
			final String id = entry.trim();
			if (id.isEmpty())
			{
				continue;
			}
			
			final int itemId = StringUtil.parseInt(id, -1);
			if (itemId < 0)
			{
				LOGGER.warning("PvpConfig: " + property + ": \"" + id + "\" is not an item id.");
				continue;
			}
			
			ids.add(itemId);
		}
		
		final int[] result = new int[ids.size()];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = ids.get(i).intValue();
		}
		
		Arrays.sort(result);
		return result;
	}
}
