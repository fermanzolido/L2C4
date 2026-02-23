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

import org.l2jmobius.gameserver.config.GeneralConfig;
import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.managers.PunishmentManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.EnchantResult;

/**
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestGiveItemToPet extends ClientPacket
{
	private int _objectId;
	private int _amount;
	
	@Override
	protected void readImpl()
	{
		_objectId = readInt();
		_amount = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if ((_amount <= 0) || (player == null) || !player.hasPet())
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You are giving items to pet too fast.");
			return;
		}
		
		if (player.getActiveEnchantItemId() != Player.ID_NONE)
		{
			player.setActiveEnchantItemId(Player.ID_NONE);
			player.sendPacket(SystemMessageId.THE_SCROLL_OF_ENCHANT_HAS_BEEN_CANCELED);
			player.sendPacket(new EnchantResult(0));
			return;
		}
		
		// Alt game - Karma punishment
		if (!PlayerConfig.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0))
		{
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.sendMessage("You cannot give items while trading.");
			return;
		}
		
		final Item item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (_amount > item.getCount())
		{
			PunishmentManager.handleIllegalPlayerAction(player, getClass().getSimpleName() + ": Character " + player.getName() + " of account " + player.getAccountName() + " tried to give item with oid " + _objectId + " to pet but has invalid count " + _amount + " item count: " + item.getCount(), GeneralConfig.DEFAULT_PUNISH);
			return;
		}
		
		if (item.isHeroItem() || !item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			player.sendPacket(SystemMessageId.ITEM_NOT_AVAILABLE_FOR_PETS);
			return;
		}
		
		final Pet pet = player.getSummon().asPet();
		if (pet.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_A_DEAD_PET);
			return;
		}
		
		if (!pet.getInventory().validateCapacity(item))
		{
			player.sendPacket(SystemMessageId.DUE_TO_THE_VOLUME_LIMIT_OF_THE_PET_S_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE);
			return;
		}
		
		if (!pet.getInventory().validateWeight(item, _amount))
		{
			player.sendPacket(SystemMessageId.EXCEEDED_PET_INVENTORY_S_WEIGHT_LIMIT);
			return;
		}
		
		if (player.transferItem(ItemProcessType.TRANSFER, _objectId, _amount, pet.getInventory(), pet) == null)
		{
			PacketLogger.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
}
