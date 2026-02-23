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
package org.l2jmobius.gameserver.model.item;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.ExtractableProduct;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.item.type.ActionType;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

/**
 * This class is dedicated to the management of EtcItem.
 */
public class EtcItem extends ItemTemplate
{
	private String _handler;
	private EtcItemType _type;
	private boolean _isBlessed;
	private List<ExtractableProduct> _extractableItems;
	
	/**
	 * Constructor for EtcItem.
	 * @param set StatSet designating the set of couples (key,value) for description of the Etc
	 */
	public EtcItem(StatSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatSet set)
	{
		super.set(set);
		_type = set.getEnum("etcitem_type", EtcItemType.class, EtcItemType.NONE);
		
		// l2j custom - EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case SOULSHOT:
			case SUMMON_SOULSHOT:
			case SUMMON_SPIRITSHOT:
			case SPIRITSHOT:
			{
				_type = EtcItemType.SHOT;
				break;
			}
			default:
			{
				break;
			}
		}
		
		_type1 = ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA;
		_type2 = ItemTemplate.TYPE2_OTHER; // default is other
		
		if (isQuestItem())
		{
			_type2 = ItemTemplate.TYPE2_QUEST;
		}
		else if ((getId() == Inventory.ADENA_ID) || (getId() == Inventory.ANCIENT_ADENA_ID))
		{
			_type2 = ItemTemplate.TYPE2_MONEY;
		}
		
		_handler = set.getString("handler", null); // ! null !
		_isBlessed = set.getBoolean("blessed", false) || (((getDefaultAction() == ActionType.SPIRITSHOT) || (getDefaultAction() == ActionType.SOULSHOT)) && (getName() != null) && getName().contains("Blessed"));
		
		// Extractable
		final String capsuled_items = set.getString("capsuled_items", null);
		if (capsuled_items != null)
		{
			final String[] split = capsuled_items.split(";");
			_extractableItems = new ArrayList<>(split.length);
			for (String part : split)
			{
				if (part.trim().isEmpty())
				{
					continue;
				}
				
				final String[] data = part.split(",");
				if (data.length != 4)
				{
					LOGGER.info("> Could not parse " + part + " in capsuled_items! item " + this);
					continue;
				}
				
				final int itemId = Integer.parseInt(data[0]);
				final int min = Integer.parseInt(data[1]);
				final int max = Integer.parseInt(data[2]);
				final double chance = Double.parseDouble(data[3]);
				if (max < min)
				{
					LOGGER.info("> Max amount < Min amount in " + part + ", item " + this);
					continue;
				}
				
				final ExtractableProduct product = new ExtractableProduct(itemId, min, max, chance);
				_extractableItems.add(product);
			}
			
			((ArrayList<?>) _extractableItems).trimToSize();
			
			// check for handler
			if (_handler == null)
			{
				LOGGER.warning("Item " + this + " define capsuled_items but missing handler.");
				_handler = "ExtractableItems";
			}
		}
		else
		{
			_extractableItems = null;
		}
	}
	
	/**
	 * @return the type of Etc Item.
	 */
	@Override
	public EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return _type.mask();
	}
	
	/**
	 * @return {@code true} if the item is an etc item, {@code false} otherwise.
	 */
	@Override
	public boolean isEtcItem()
	{
		return true;
	}
	
	/**
	 * @return the handler name, null if no handler for item.
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	/**
	 * @return {@code true} if the item is blessed, {@code false} otherwise.
	 */
	public boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return the extractable items list.
	 */
	public List<ExtractableProduct> getExtractableItems()
	{
		return _extractableItems;
	}
}
