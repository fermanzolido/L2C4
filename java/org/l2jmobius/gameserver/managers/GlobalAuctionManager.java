package org.l2jmobius.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.enums.ItemLocation;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;

/**
 * Global Auction House Manager
 */
public class GlobalAuctionManager
{
	private static final Logger LOGGER = Logger.getLogger(GlobalAuctionManager.class.getName());

	private final Map<Integer, AuctionListing> _auctions = new ConcurrentHashMap<>();
	private final Map<Integer, Long> _funds = new ConcurrentHashMap<>();

	private static final GlobalAuctionManager _instance = new GlobalAuctionManager();

	public static GlobalAuctionManager getInstance()
	{
		return _instance;
	}

	private GlobalAuctionManager()
	{
		load();
	}

	private void load()
	{
		// Load funds
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT player_id, adena FROM global_auction_funds");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				_funds.put(rs.getInt("player_id"), rs.getLong("adena"));
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to load global auction funds.", e);
		}

		// Load auctions
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM global_auctions");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int auctionId = rs.getInt("id");
				final int sellerId = rs.getInt("seller_id");
				final int itemObjectId = rs.getInt("item_object_id");
				final long price = rs.getLong("price");
				final long endTime = rs.getLong("end_time");

				// Restore item from DB
				final Item item = loadItem(itemObjectId);
				if (item != null)
				{
					final AuctionListing listing = new AuctionListing(auctionId, sellerId, item, price, endTime);
					_auctions.put(auctionId, listing);
				}
				else
				{
					LOGGER.warning("GlobalAuctionManager: Found auction " + auctionId + " without item " + itemObjectId + ". Cleaning up.");
					// Clean up orphan auction
					try (PreparedStatement del = con.prepareStatement("DELETE FROM global_auctions WHERE id=?"))
					{
						del.setInt(1, auctionId);
						del.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to load global auctions.", e);
		}

		LOGGER.info("GlobalAuctionManager: Loaded " + _auctions.size() + " auctions and " + _funds.size() + " fund accounts.");
	}

	private Item loadItem(int objectId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM items WHERE object_id = ?"))
		{
			ps.setInt(1, objectId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return Item.restoreFromDb(rs.getInt("owner_id"), rs);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to load item for auction " + objectId, e);
		}
		return null;
	}

	public synchronized boolean addListing(Player player, int objectId, long price, int days)
	{
		if (price <= 0)
		{
			player.sendMessage("Price must be positive.");
			return false;
		}

		final Item item = player.getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			player.sendMessage("Item not found.");
			return false;
		}

		if (!item.isTradeable() || item.isEquipped() || item.isQuestItem())
		{
			player.sendMessage("This item cannot be auctioned.");
			return false;
		}

		// Remove from inventory
		final Item droppedItem = player.getInventory().dropItem(ItemProcessType.TRANSFER, item, player, null);
		if (droppedItem == null)
		{
			player.sendMessage("Failed to remove item from inventory.");
			return false;
		}

		// Move to AUCTION location
		droppedItem.setItemLocation(ItemLocation.AUCTION);
		droppedItem.updateDatabase();

		// Insert into DB
		long endTime = System.currentTimeMillis() + (days * 86400000L);
		int auctionId = 0;

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO global_auctions (seller_id, item_object_id, price, end_time) VALUES (?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, droppedItem.getObjectId());
			ps.setLong(3, price);
			ps.setLong(4, endTime);
			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys())
			{
				if (rs.next())
				{
					auctionId = rs.getInt(1);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to add auction listing.", e);
			// Rollback item location
			droppedItem.setItemLocation(ItemLocation.INVENTORY);
			droppedItem.updateDatabase();
			player.getInventory().addItem(ItemProcessType.TRANSFER, droppedItem, player, null);
			return false;
		}

		if (auctionId > 0)
		{
			final AuctionListing listing = new AuctionListing(auctionId, player.getObjectId(), droppedItem, price, endTime);
			_auctions.put(auctionId, listing);
			player.sendInventoryUpdate(new InventoryUpdate());
			player.sendMessage("Item listed for auction.");
			return true;
		}

		return false;
	}

	public synchronized boolean purchaseItem(Player buyer, int auctionId)
	{
		final AuctionListing listing = _auctions.get(auctionId);
		if (listing == null)
		{
			buyer.sendMessage("Auction not found or expired.");
			return false;
		}

		if (listing.getSellerId() == buyer.getObjectId())
		{
			buyer.sendMessage("You cannot buy your own item.");
			return false;
		}

		if (buyer.getInventory().getAdena() < listing.getPrice())
		{
			buyer.sendMessage("Not enough Adena.");
			return false;
		}

		// Take Adena
		if (!buyer.destroyItemByItemId(ItemProcessType.BUY, 57, (int) listing.getPrice(), buyer, true))
		{
			return false;
		}

		// Give funds to seller
		addFunds(listing.getSellerId(), listing.getPrice());

		// Give item to buyer
		final Item item = listing.getItem();
		item.setOwnerId(buyer.getObjectId());
		item.setItemLocation(ItemLocation.INVENTORY);
		item.updateDatabase();

		buyer.getInventory().addItem(ItemProcessType.BUY, item, buyer, null);
		buyer.sendInventoryUpdate(new InventoryUpdate());

		// Remove listing
		removeAuction(auctionId);

		buyer.sendMessage("You purchased " + item.getName() + ".");

		// Notify seller if online? (Optional)
		return true;
	}

	public synchronized boolean cancelListing(Player player, int auctionId)
	{
		final AuctionListing listing = _auctions.get(auctionId);
		if (listing == null)
		{
			player.sendMessage("Auction not found.");
			return false;
		}

		if (listing.getSellerId() != player.getObjectId() && !player.isGM())
		{
			player.sendMessage("You do not own this auction.");
			return false;
		}

		// Return item
		final Item item = listing.getItem();
		item.setOwnerId(player.getObjectId()); // Should be already set, but confirm
		item.setItemLocation(ItemLocation.INVENTORY);
		item.updateDatabase();

		player.getInventory().addItem(ItemProcessType.RESTORE, item, player, null);
		player.sendInventoryUpdate(new InventoryUpdate());

		// Remove listing
		removeAuction(auctionId);

		player.sendMessage("Auction cancelled. Item returned to inventory.");
		return true;
	}

	private void removeAuction(int auctionId)
	{
		_auctions.remove(auctionId);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM global_auctions WHERE id=?"))
		{
			ps.setInt(1, auctionId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to delete auction " + auctionId, e);
		}
	}

	private void addFunds(int playerId, long amount)
	{
		long current = _funds.getOrDefault(playerId, 0L);
		long newVal = current + amount;
		_funds.put(playerId, newVal);

		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO global_auction_funds (player_id, adena) VALUES (?, ?) ON DUPLICATE KEY UPDATE adena=?"))
		{
			ps.setInt(1, playerId);
			ps.setLong(2, newVal);
			ps.setLong(3, newVal);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to update funds for " + playerId, e);
		}
	}

	public synchronized long collectFunds(Player player)
	{
		final int playerId = player.getObjectId();
		final long amount = _funds.getOrDefault(playerId, 0L);

		if (amount <= 0)
		{
			return 0;
		}

		// Reset funds
		_funds.put(playerId, 0L);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE global_auction_funds SET adena=0 WHERE player_id=?"))
		{
			ps.setInt(1, playerId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to reset funds for " + playerId, e);
			// Rollback logic would be complex here, assume DB works
		}

		player.addAdena(ItemProcessType.RESTORE, (int) amount, null, true);
		return amount;
	}

	public List<AuctionListing> getAllAuctions()
	{
		return new ArrayList<>(_auctions.values());
	}

	public long getFunds(int playerId)
	{
		return _funds.getOrDefault(playerId, 0L);
	}

	public static class AuctionListing
	{
		private final int _id;
		private final int _sellerId;
		private final Item _item;
		private final long _price;
		private final long _endTime;

		public AuctionListing(int id, int sellerId, Item item, long price, long endTime)
		{
			_id = id;
			_sellerId = sellerId;
			_item = item;
			_price = price;
			_endTime = endTime;
		}

		public int getId()
		{
			return _id;
		}

		public int getSellerId()
		{
			return _sellerId;
		}

		public Item getItem()
		{
			return _item;
		}

		public long getPrice()
		{
			return _price;
		}

		public long getEndTime()
		{
			return _endTime;
		}
	}
}
