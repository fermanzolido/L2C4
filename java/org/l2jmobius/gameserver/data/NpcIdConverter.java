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
package org.l2jmobius.gameserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Mobius
 */
public class NpcIdConverter
{
	private static final Logger LOGGER = Logger.getLogger(NpcIdConverter.class.getName());
	
	private static final Map<Integer, Integer> CT0_TO_C4_IDS = new HashMap<>();
	
	public static void init()
	{
		final File file = new File("data/stats/npcs/CT0_to_C4_ids.txt");
		if (!file.exists())
		{
			LOGGER.warning(NpcIdConverter.class.getSimpleName() + ": File not found: " + file.getAbsolutePath());
			return;
		}
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				
				final String[] parts = line.split(";");
				CT0_TO_C4_IDS.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			}
			
			LOGGER.info(NpcIdConverter.class.getSimpleName() + ": Loaded " + CT0_TO_C4_IDS.size() + " replacements.");
		}
		catch (IOException e)
		{
			LOGGER.warning(NpcIdConverter.class.getSimpleName() + ": Error reading file: " + file.getAbsolutePath());
		}
	}
	
	public static boolean exists(int npcId)
	{
		return CT0_TO_C4_IDS.containsKey(npcId);
	}
	
	public static int convert(int npcId)
	{
		final int id = CT0_TO_C4_IDS.getOrDefault(npcId, -1);
		if (id == -1)
		{
			// LOGGER.warning(NpcIdConverter.class.getSimpleName() + ": Could not find proper NPC id for: " + npcId);
			return npcId;
		}
		
		return id;
	}
}
