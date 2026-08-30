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
import org.l2jmobius.gameserver.config.PlayerConfig;
import org.l2jmobius.gameserver.model.World;
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
					final Item item = Item.restoreFromDb(rs.getInt("owner_id"), rs);
					if (item != null)
					{
						// Every other place that restores an item registers it, and this one did
						// not, so after a restart the listed items were objects the world did not
						// know about and stayed that way once a buyer received them.
						World.getInstance().addObject(item);
					}

					return item;
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
			player.addItem(ItemProcessType.TRANSFER, droppedItem, null, true);
			return false;
		}

		if (auctionId > 0)
		{
			final AuctionListing listing = new AuctionListing(auctionId, player.getObjectId(), droppedItem, price, endTime);
			_auctions.put(auctionId, listing);

			// The update was sent empty, so the client kept showing an item the inventory no
			// longer held until the next relog.
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(droppedItem);
			player.sendInventoryUpdate(iu);

			player.sendMessage("Item listed for auction.");
			return true;
		}

		// Insert reported no generated key. Without this the row exists but no listing does,
		// leaving the item out of the inventory and out of reach of its own owner until the
		// next restart reloads it. Same rollback the failure above performs.
		LOGGER.severe("GlobalAuctionManager: No auction id returned for item " + droppedItem.getObjectId() + ". Returning it to " + player.getName() + ".");
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM global_auctions WHERE item_object_id=?"))
		{
			ps.setInt(1, droppedItem.getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to clean up auction row for item " + droppedItem.getObjectId(), e);
		}

		droppedItem.setItemLocation(ItemLocation.INVENTORY);
		droppedItem.updateDatabase();
		player.addItem(ItemProcessType.TRANSFER, droppedItem, null, true);
		player.sendMessage("Failed to list the item for auction.");
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

		buyer.addItem(ItemProcessType.BUY, item, null, true);

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

		// The check above lets a GM cancel someone else's listing, and everything below used
		// to hand the item to whoever ran the command, so moderating a listing quietly
		// transferred it to the GM. It goes back to the seller, who has to be online for that.
		final Player seller = listing.getSellerId() == player.getObjectId() ? player : World.getInstance().getPlayer(listing.getSellerId());
		if (seller == null)
		{
			player.sendMessage("The seller must be online to receive the returned item.");
			return false;
		}

		// Return item
		final Item item = listing.getItem();
		item.setOwnerId(seller.getObjectId());
		item.setItemLocation(ItemLocation.INVENTORY);
		item.updateDatabase();

		seller.addItem(ItemProcessType.TRANSFER, item, null, true);

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

		// Funds accumulate across sales in a long, while addAdena takes an int and clamps what
		// it hands over to the adena cap. Casting the whole balance turned a seller past two
		// billion into a negative count, and the balance was being cleared before the credit,
		// so whatever the cap refused was destroyed rather than left to collect later.
		final long room = (long) PlayerConfig.MAX_ADENA - player.getInventory().getAdena();
		final long paid = Math.min(amount, room);
		if (paid <= 0)
		{
			player.sendMessage("You cannot carry any more Adena.");
			return 0;
		}

		// Credited first, then only what was credited is taken off the balance. paid never
		// exceeds the cap, so this cast cannot lose anything.
		player.addAdena(ItemProcessType.RESTORE, (int) paid, null, true);

		final long remaining = amount - paid;
		_funds.put(playerId, remaining);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE global_auction_funds SET adena=? WHERE player_id=?"))
		{
			ps.setLong(1, remaining);
			ps.setInt(2, playerId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to update funds for " + playerId, e);
		}

		return paid;
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
