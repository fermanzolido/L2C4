package org.l2jmobius.gameserver.model.actor.instance;

import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.l2jmobius.gameserver.managers.GlobalAuctionManager;
import org.l2jmobius.gameserver.managers.GlobalAuctionManager.AuctionListing;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.enums.creature.InstanceType;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class GlobalAuctioneer extends Npc
{
	public GlobalAuctioneer(NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.Npc);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null)
		{
			return;
		}

		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();

		if (actualCommand.equalsIgnoreCase("auction_list"))
		{
			int page = 1;
			String search = "";
			if (st.hasMoreTokens())
			{
				try
				{
					page = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException e)
				{
					page = 1;
				}
			}
			if (st.hasMoreTokens())
			{
				search = command.substring(command.indexOf(String.valueOf(page)) + String.valueOf(page).length()).trim();
			}
			showAuctionList(player, page, search);
		}
		else if (actualCommand.equalsIgnoreCase("auction_sell_form"))
		{
			showSellForm(player);
		}
		else if (actualCommand.equalsIgnoreCase("auction_sell_confirm"))
		{
			if (st.hasMoreTokens())
			{
				try
				{
					final int objectId = Integer.parseInt(st.nextToken());
					showSellConfirm(player, objectId);
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("Invalid item.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("auction_sell"))
		{
			if (st.countTokens() >= 2)
			{
				try
				{
					final int objectId = Integer.parseInt(st.nextToken());
					final long price = Long.parseLong(st.nextToken());
					if (GlobalAuctionManager.getInstance().addListing(player, objectId, price, 7)) // 7 days default
					{
						showChatWindow(player);
					}
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("Invalid format.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("auction_buy"))
		{
			if (st.hasMoreTokens())
			{
				try
				{
					final int auctionId = Integer.parseInt(st.nextToken());
					if (GlobalAuctionManager.getInstance().purchaseItem(player, auctionId))
					{
						showAuctionList(player, 1, "");
					}
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("Invalid auction.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("auction_my"))
		{
			showMyAuctions(player);
		}
		else if (actualCommand.equalsIgnoreCase("auction_cancel"))
		{
			if (st.hasMoreTokens())
			{
				try
				{
					final int auctionId = Integer.parseInt(st.nextToken());
					if (GlobalAuctionManager.getInstance().cancelListing(player, auctionId))
					{
						showMyAuctions(player);
					}
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("Invalid auction.");
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("auction_collect"))
		{
			final long amount = GlobalAuctionManager.getInstance().collectFunds(player);
			if (amount > 0)
			{
				player.sendMessage("You collected " + amount + " Adena.");
				showMyAuctions(player);
			}
			else
			{
				player.sendMessage("No funds to collect.");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Global Auction</title>");
		sb.append("<center>");
		sb.append("<br><font color=\"LEVEL\">Welcome to the Global Auction House.</font><br>");
		sb.append("<img src=\"L2UI.SquareWhite\" width=260 height=1><br><br>");
		sb.append("<button value=\"Buy Items\" action=\"bypass -h npc_%objectId%_auction_list 1\" width=135 height=21 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"><br>");
		sb.append("<button value=\"Sell Item\" action=\"bypass -h npc_%objectId%_auction_sell_form\" width=135 height=21 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"><br>");
		sb.append("<button value=\"My Auctions\" action=\"bypass -h npc_%objectId%_auction_my\" width=135 height=21 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"><br>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showAuctionList(Player player, int page, String search)
	{
		final List<AuctionListing> allAuctions = GlobalAuctionManager.getInstance().getAllAuctions();
		final String lowerSearch = search.toLowerCase();

		final List<AuctionListing> filtered = allAuctions.stream()
			.filter(a -> search.isEmpty() || a.getItem().getName().toLowerCase().contains(lowerSearch))
			.sorted(Comparator.comparingLong(AuctionListing::getEndTime))
			.collect(Collectors.toList());

		final int limit = 10;
		final int maxPage = (int) Math.ceil((double) filtered.size() / limit);
		final int currentPage = Math.max(1, Math.min(page, maxPage == 0 ? 1 : maxPage));
		final int start = (currentPage - 1) * limit;
		final int end = Math.min(start + limit, filtered.size());

		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Auction List</title>");
		sb.append("<table width=260><tr>");
		sb.append("<td width=50><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"></td>");
		sb.append("<td width=160 align=center>Page " + currentPage + "/" + (maxPage == 0 ? 1 : maxPage) + "</td>");
		sb.append("<td width=50><button value=\"My\" action=\"bypass -h npc_%objectId%_auction_my\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"></td>");
		sb.append("</tr></table>");

		sb.append("<br><table width=260 border=0>");
		// Search box
		sb.append("<tr><td>Search: <edit var=\"search\" width=150><button value=\"Go\" action=\"bypass -h npc_%objectId%_auction_list 1 $search\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"></td></tr>");
		sb.append("</table><br>");

		sb.append("<img src=\"L2UI.SquareWhite\" width=260 height=1>");
		sb.append("<table width=260 border=0>");

		if (filtered.isEmpty())
		{
			sb.append("<tr><td>No items found.</td></tr>");
		}
		else
		{
			for (int i = start; i < end; i++)
			{
				final AuctionListing listing = filtered.get(i);
				final Item item = listing.getItem();
				final String icon = item.getTemplate().getIcon(); // L2J Mobius ItemTemplate has getIcon? Check later. Usually yes or toString().

				sb.append("<tr>");
				sb.append("<td width=32><img src=\"" + (icon != null ? icon : "icon.etc_question_mark_i00") + "\" width=32 height=32></td>");
				sb.append("<td width=148>");
				sb.append("<font color=\"LEVEL\">" + item.getName() + (item.getEnchantLevel() > 0 ? " +" + item.getEnchantLevel() : "") + "</font><br1>");
				sb.append("Count: " + item.getCount() + "<br1>");
				sb.append("Price: " + listing.getPrice());
				sb.append("</td>");
				sb.append("<td width=80><button value=\"Buy\" action=\"bypass -h npc_%objectId%_auction_buy " + listing.getId() + "\" width=75 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"></td>");
				sb.append("</tr>");
				sb.append("<tr><td colspan=3><img src=\"L2UI.SquareWhite\" width=260 height=1></td></tr>");
			}
		}
		sb.append("</table>");

		// Pagination
		sb.append("<br><table width=260><tr>");
		if (currentPage > 1)
		{
			sb.append("<td align=left><a action=\"bypass -h npc_%objectId%_auction_list " + (currentPage - 1) + " " + search + "\">Prev</a></td>");
		}
		else
		{
			sb.append("<td align=left>Prev</td>");
		}

		if (currentPage < maxPage)
		{
			sb.append("<td align=right><a action=\"bypass -h npc_%objectId%_auction_list " + (currentPage + 1) + " " + search + "\">Next</a></td>");
		}
		else
		{
			sb.append("<td align=right>Next</td>");
		}
		sb.append("</tr></table>");

		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private void showSellForm(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Sell Item</title>");
		sb.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"><br>");
		sb.append("Choose an item to sell:<br>");
		sb.append("<img src=\"L2UI.SquareWhite\" width=260 height=1>");
		sb.append("<table width=260>");

		for (Item item : player.getInventory().getItems())
		{
			if (item.isTradeable() && !item.isEquipped() && !item.isQuestItem() && (item.getId() != 57))
			{
				sb.append("<tr>");
				sb.append("<td width=32><img src=\"" + item.getTemplate().getIcon() + "\" width=32 height=32></td>");
				sb.append("<td width=228><a action=\"bypass -h npc_%objectId%_auction_sell_confirm " + item.getObjectId() + "\">" + item.getName() + (item.getEnchantLevel() > 0 ? " +" + item.getEnchantLevel() : "") + " (" + item.getCount() + ")</a></td>");
				sb.append("</tr>");
			}
		}

		sb.append("</table></body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private void showSellConfirm(Player player, int objectId)
	{
		final Item item = player.getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			showSellForm(player);
			return;
		}

		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Sell Confirm</title>");
		sb.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_auction_sell_form\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"><br>");
		sb.append("Selling: <font color=\"LEVEL\">" + item.getName() + "</font><br>");
		sb.append("Enter Price (Adena):<br>");
		sb.append("<edit var=\"price\" width=150 type=number><br>");
		sb.append("<button value=\"Confirm Sale\" action=\"bypass -h npc_%objectId%_auction_sell " + objectId + " $price\" width=135 height=21 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\">");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private void showMyAuctions(Player player)
	{
		final long funds = GlobalAuctionManager.getInstance().getFunds(player.getObjectId());
		final List<AuctionListing> myAuctions = GlobalAuctionManager.getInstance().getAllAuctions().stream()
			.filter(a -> a.getSellerId() == player.getObjectId())
			.collect(Collectors.toList());

		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>My Auctions</title>");
		sb.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat\" width=50 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"><br>");
		sb.append("Available Funds: <font color=\"LEVEL\">" + funds + "</font> Adena<br>");
		if (funds > 0)
		{
			sb.append("<button value=\"Collect Funds\" action=\"bypass -h npc_%objectId%_auction_collect\" width=135 height=21 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\"><br>");
		}

		sb.append("<img src=\"L2UI.SquareWhite\" width=260 height=1><br>");
		sb.append("Your Listings:<br>");
		sb.append("<table width=260>");

		if (myAuctions.isEmpty())
		{
			sb.append("<tr><td>You have no active listings.</td></tr>");
		}
		else
		{
			for (AuctionListing listing : myAuctions)
			{
				final Item item = listing.getItem();
				sb.append("<tr>");
				sb.append("<td width=150>" + item.getName() + " (" + listing.getPrice() + " A)</td>");
				sb.append("<td width=100><button value=\"Cancel\" action=\"bypass -h npc_%objectId%_auction_cancel " + listing.getId() + "\" width=60 height=21 back=\"L2UI_CH3.smallbutton2_down\" fore=\"L2UI_CH3.smallbutton2\"></td>");
				sb.append("</tr>");
			}
		}

		sb.append("</table></body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
