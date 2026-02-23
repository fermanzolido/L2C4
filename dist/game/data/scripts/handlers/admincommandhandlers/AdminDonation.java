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
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.xml.ItemData;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;

/**
 * Donation Management Admin Handler.
 * Allows admins to reward donors even when they are offline.
 * 
 * @author Antigravity
 */
public class AdminDonation implements IAdminCommandHandler {
    private static final Logger LOGGER = Logger.getLogger(AdminDonation.class.getName());

    private static final String[] ADMIN_COMMANDS = {
            "admin_donation",
            "admin_donate"
    };

    @Override
    public boolean onCommand(String command, Player activeChar) {
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();

        if (actualCommand.equalsIgnoreCase("admin_donation")) {
            AdminHtml.showAdminHtml(activeChar, "donation.htm");
        } else if (actualCommand.equalsIgnoreCase("admin_donate")) {
            if (st.countTokens() < 3) {
                activeChar.sendSysMessage("Usage: //donate <player_name> <item_id> <amount> [enchant]");
                return false;
            }

            final String targetName = st.nextToken();
            final int itemId = Integer.parseInt(st.nextToken());
            final int amount = Integer.parseInt(st.nextToken());
            final int enchant = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;

            final int targetId = CharInfoTable.getInstance().getIdByName(targetName);
            if (targetId <= 0) {
                activeChar.sendSysMessage("Player " + targetName + " not found.");
                return false;
            }

            final ItemTemplate template = ItemData.getInstance().getTemplate(itemId);
            if (template == null) {
                activeChar.sendSysMessage("Item ID " + itemId + " does not exist.");
                return false;
            }

            processDonation(activeChar, targetName, targetId, itemId, amount, enchant);
        }

        return true;
    }

    private void processDonation(Player admin, String targetName, int targetId, int itemId, int amount, int enchant) {
        final Player target = World.getInstance().getPlayer(targetId);
        boolean delivered = false;

        if ((target != null) && target.isOnline()) {
            final org.l2jmobius.gameserver.model.item.instance.Item item = target.addItem(ItemProcessType.REWARD,
                    itemId, amount, admin, true);
            if ((item != null) && (enchant > 0) && item.isEnchantable()) {
                item.setEnchantLevel(enchant);
            }
            target.sendMessage("You have received a donation reward: " + amount + " "
                    + ItemData.getInstance().getTemplate(itemId).getName());
            admin.sendSysMessage("Donation delivered to " + targetName + " (Online).");
            delivered = true;
        } else {
            admin.sendSysMessage("Player " + targetName + " is offline. Donation will be delivered on next login.");
        }

        logDonation(admin.getName(), targetName, targetId, itemId, amount, enchant, delivered);

        // Show confirmation UI
        final String content = org.l2jmobius.gameserver.cache.HtmCache.getInstance().getHtm(admin,
                "data/html/admin/donation_confirm.htm");
        if (content != null) {
            final org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage html = new org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage();
            html.setHtml(content);
            html.replace("%target%", targetName);
            html.replace("%item_name%", ItemData.getInstance().getTemplate(itemId).getName());
            html.replace("%amount%", String.valueOf(amount));
            html.replace("%status%", delivered ? "<font color=\"00FF00\">Delivered (Online)</font>"
                    : "<font color=\"FFFF00\">Pending (Offline)</font>");
            admin.sendPacket(html);
        }
    }

    private void logDonation(String adminName, String targetName, int targetId, int itemId, int amount, int enchant,
            boolean delivered) {
        try (Connection con = DatabaseFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO donations_log (admin_name, target_name, target_id, item_id, amount, enchant, delivered) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, adminName);
            ps.setString(2, targetName);
            ps.setInt(3, targetId);
            ps.setInt(4, itemId);
            ps.setLong(5, amount);
            ps.setInt(6, enchant);
            ps.setInt(7, delivered ? 1 : 0);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not log donation!", e);
        }
    }

    @Override
    public String[] getCommandList() {
        return ADMIN_COMMANDS;
    }
}
