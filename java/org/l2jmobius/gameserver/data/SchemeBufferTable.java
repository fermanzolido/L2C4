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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.config.custom.SchemeBufferConfig;
import org.l2jmobius.gameserver.model.actor.holders.npc.BuffSkillHolder;

/**
 * This class loads available skills and stores players' buff schemes into _schemesTable.
 */
public class SchemeBufferTable
{
	private static final Logger LOGGER = Logger.getLogger(SchemeBufferTable.class.getName());
	
	private static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";
	private static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";
	private static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	
	private final Map<Integer, Map<String, List<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();
	
	public SchemeBufferTable()
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new File("./data/SchemeBufferSkills.xml"));
			final Node n = document.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!d.getNodeName().equalsIgnoreCase("category"))
				{
					continue;
				}
				
				final String category = d.getAttributes().getNamedItem("type").getNodeValue();
				for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
				{
					if (!c.getNodeName().equalsIgnoreCase("buff"))
					{
						continue;
					}
					
					final NamedNodeMap attrs = c.getAttributes();
					final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					_availableBuffs.put(skillId, new BuffSkillHolder(skillId, Integer.parseInt(attrs.getNamedItem("level").getNodeValue()), Integer.parseInt(attrs.getNamedItem("price").getNodeValue()), category, attrs.getNamedItem("desc").getNodeValue()));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("SchemeBufferTable: Failed to load buff info : " + e);
		}
		
		int count = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement(LOAD_SCHEMES);
			ResultSet rs = st.executeQuery())
		{
			while (rs.next())
			{
				final int objectId = rs.getInt("object_id");
				final String schemeName = rs.getString("scheme_name");
				final String[] skills = rs.getString("skills").split(",");
				final List<Integer> schemeList = new ArrayList<>();
				for (String skill : skills)
				{
					// Don't feed the skills list if the list is empty.
					if (skill.isEmpty())
					{
						break;
					}
					
					final Integer skillId = Integer.parseInt(skill);
					if (_availableBuffs.containsKey(skillId))
					{
						schemeList.add(skillId);
					}
				}
				
				setScheme(objectId, schemeName, schemeList);
				count++;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("SchemeBufferTable: Failed to load buff schemes: " + e);
		}
		
		LOGGER.info("SchemeBufferTable: Loaded " + count + " players schemes and " + _availableBuffs.size() + " available buffs.");
	}
	
	public void saveSchemes()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// Delete all entries from database.
			try (PreparedStatement st = con.prepareStatement(DELETE_SCHEMES))
			{
				st.execute();
			}
			
			// Save _schemesTable content.
			try (PreparedStatement st = con.prepareStatement(INSERT_SCHEME))
			{
				for (Entry<Integer, Map<String, List<Integer>>> player : _schemesTable.entrySet())
				{
					for (Entry<String, List<Integer>> scheme : player.getValue().entrySet())
					{
						// Build a String composed of skill ids seperated by a ",".
						final StringBuilder sb = new StringBuilder();
						for (int skillId : scheme.getValue())
						{
							sb.append(skillId + ",");
						}
						
						// Delete the last "," : must be called only if there is something to delete !
						if (sb.length() > 0)
						{
							sb.setLength(sb.length() - 1);
						}
						
						st.setInt(1, player.getKey());
						st.setString(2, scheme.getKey());
						st.setString(3, sb.toString());
						st.addBatch();
					}
				}
				
				st.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("BufferTableScheme: Error while saving schemes : " + e);
		}
	}
	
	public void setScheme(int playerId, String schemeName, List<Integer> list)
	{
		if (!_schemesTable.containsKey(playerId))
		{
			_schemesTable.put(playerId, new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		}
		else if (_schemesTable.get(playerId).size() >= SchemeBufferConfig.BUFFER_MAX_SCHEMES)
		{
			return;
		}
		
		_schemesTable.get(playerId).put(schemeName, list);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @return the list of schemes for a given player.
	 */
	public Map<String, List<Integer>> getPlayerSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @return the List holding skills for the given scheme name and player, or null (if scheme or player isn't registered).
	 */
	public List<Integer> getScheme(int playerId, String schemeName)
	{
		if ((_schemesTable.get(playerId) == null) || (_schemesTable.get(playerId).get(schemeName) == null))
		{
			return Collections.emptyList();
		}
		
		return _schemesTable.get(playerId).get(schemeName);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @param skillId : The skill id to check.
	 * @return true if the skill is already registered on the scheme, or false otherwise.
	 */
	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		final List<Integer> skills = getScheme(playerId, schemeName);
		if (skills.isEmpty())
		{
			return false;
		}
		
		for (int id : skills)
		{
			if (id == skillId)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param groupType : The type of skills to return.
	 * @return a list of skills ids based on the given groupType.
	 */
	public List<Integer> getSkillsIdsByType(String groupType)
	{
		final List<Integer> skills = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (skill.getType().equalsIgnoreCase(groupType))
			{
				skills.add(skill.getId());
			}
		}
		
		return skills;
	}
	
	/**
	 * @return a list of all buff types available.
	 */
	public List<String> getSkillTypes()
	{
		final List<String> skillTypes = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (!skillTypes.contains(skill.getType()))
			{
				skillTypes.add(skill.getType());
			}
		}
		
		return skillTypes;
	}
	
	public BuffSkillHolder getAvailableBuff(int skillId)
	{
		return _availableBuffs.get(skillId);
	}
	
	public static SchemeBufferTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SchemeBufferTable INSTANCE = new SchemeBufferTable();
	}
}
