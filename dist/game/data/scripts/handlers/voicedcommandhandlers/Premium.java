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
package handlers.voicedcommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.l2jmobius.gameserver.config.RatesConfig;
import org.l2jmobius.gameserver.config.custom.DualboxCheckConfig;
import org.l2jmobius.gameserver.config.custom.PremiumSystemConfig;
import org.l2jmobius.gameserver.config.custom.SchemeBufferConfig;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.managers.PremiumManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Premium implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"premium"
	};

	/** Adena is rated by item id rather than by the generic drop rate, so it is read from the by-id maps. */
	private static final int ADENA_ID = 57;

	@Override
	public boolean onCommand(String command, Player activeChar, String target)
	{
		if (!command.startsWith("premium") || !PremiumSystemConfig.PREMIUM_SYSTEM_ENABLED)
		{
			return false;
		}

		final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		final long now = System.currentTimeMillis();
		final long endDate = PremiumManager.getInstance().getPremiumExpiration(activeChar.getAccountName());

		// Premium that runs out mid-session leaves its row in place until the next login clears
		// it, and the expire task only drops the in-memory flag. Deciding the status against the
		// clock rather than against the mere presence of a date keeps this page from reporting
		// Premium to someone the server has already stopped rewarding.
		final boolean isPremium = endDate > now;

		// Only what premium actually changes is listed. A multiplier of 1 leaves the server rate
		// exactly where it was, so printing it would advertise a benefit that is not delivered.
		final StringBuilder benefits = new StringBuilder();
		appendRate(benefits, "Rate XP", RatesConfig.RATE_XP, PremiumSystemConfig.PREMIUM_RATE_XP);
		appendRate(benefits, "Rate SP", RatesConfig.RATE_SP, PremiumSystemConfig.PREMIUM_RATE_SP);
		appendRate(benefits, "Drop Chance", RatesConfig.RATE_DEATH_DROP_CHANCE_MULTIPLIER, PremiumSystemConfig.PREMIUM_RATE_DROP_CHANCE);
		appendRate(benefits, "Drop Amount", RatesConfig.RATE_DEATH_DROP_AMOUNT_MULTIPLIER, PremiumSystemConfig.PREMIUM_RATE_DROP_AMOUNT);
		appendRate(benefits, "Spoil Chance", RatesConfig.RATE_SPOIL_DROP_CHANCE_MULTIPLIER, PremiumSystemConfig.PREMIUM_RATE_SPOIL_CHANCE);
		appendRate(benefits, "Spoil Amount", RatesConfig.RATE_SPOIL_DROP_AMOUNT_MULTIPLIER, PremiumSystemConfig.PREMIUM_RATE_SPOIL_AMOUNT);
		appendRate(benefits, "Quest XP", RatesConfig.RATE_QUEST_REWARD_XP, PremiumSystemConfig.PREMIUM_RATE_QUEST_XP);
		appendRate(benefits, "Quest SP", RatesConfig.RATE_QUEST_REWARD_SP, PremiumSystemConfig.PREMIUM_RATE_QUEST_SP);
		appendRate(benefits, "Adena Chance", rateForAdena(RatesConfig.RATE_DROP_CHANCE_BY_ID, RatesConfig.RATE_DEATH_DROP_CHANCE_MULTIPLIER), rateForAdena(PremiumSystemConfig.PREMIUM_RATE_DROP_CHANCE_BY_ID, PremiumSystemConfig.PREMIUM_RATE_DROP_CHANCE));
		appendRate(benefits, "Adena Amount", rateForAdena(RatesConfig.RATE_DROP_AMOUNT_BY_ID, RatesConfig.RATE_DEATH_DROP_AMOUNT_MULTIPLIER), rateForAdena(PremiumSystemConfig.PREMIUM_RATE_DROP_AMOUNT_BY_ID, PremiumSystemConfig.PREMIUM_RATE_DROP_AMOUNT));
		if (DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PREMIUM_PER_IP > DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PER_IP)
		{
			appendPerk(benefits, "Clients per IP", String.valueOf(DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PREMIUM_PER_IP), String.valueOf(DualboxCheckConfig.DUALBOX_CHECK_MAX_PLAYERS_PER_IP));
		}
		appendPerk(benefits, "Scheme Buffer", "any level", "up to level " + SchemeBufferConfig.BUFFER_FREE_UNTIL_LEVEL);

		final StringBuilder html = new StringBuilder();
		html.append("<html><body><title>Account Details</title><center>");
		html.append("<table>");
		html.append("<tr><td><center>Account Status: <font color=\"LEVEL\">" + (isPremium ? "Premium" : "Normal") + "</font><br></td></tr>");
		if (isPremium)
		{
			html.append("<tr><td>Expires: <font color=\"00A5FF\">" + format.format(endDate) + "</font><br1></td></tr>");
			html.append("<tr><td>Current Date: <font color=\"70FFCA\">" + format.format(now) + "</font><br></td></tr>");
		}
		html.append("<tr><td><center>" + (isPremium ? "Your Benefits" : "Premium Benefits") + "</center><br></td></tr>");
		if (benefits.length() == 0)
		{
			html.append("<tr><td><font color=\"909090\">No benefits are configured.</font><br></td></tr>");
		}
		else
		{
			html.append(benefits);
		}
		html.append("<tr><td><br><center>Premium Info & Rules</center><br></td></tr>");
		html.append("<tr><td><font color=\"70FFCA\">1. Premium benefits CAN NOT BE TRANSFERED.</font><br1></td></tr>");
		html.append("<tr><td><font color=\"70FFCA\">2. Premium does not effect party members.</font><br1></td></tr>");
		html.append("<tr><td><font color=\"70FFCA\">3. Premium benefits effect ALL characters in same account.</font><br></td></tr>");
		if (isPremium)
		{
			html.append("<tr><td><br><center>Thank you for supporting our server.</center></td></tr>");
		}
		else
		{
			html.append("<tr><td><br><center><font color=\"CDB67F\">Premium is not sold in game.</font><br1><font color=\"CDB67F\">Ask the server administration.</font></center></td></tr>");
		}
		html.append("</table>");
		html.append("</center></body></html>");

		final NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(html.toString());
		activeChar.sendPacket(msg);
		return true;
	}

	/**
	 * Appends a rate row, but only when premium actually raises it.
	 * @param html the buffer to append to
	 * @param label the rate name shown to the player
	 * @param base the rate a normal account gets
	 * @param multiplier the premium multiplier applied on top of it
	 */
	private static void appendRate(StringBuilder html, String label, float base, float multiplier)
	{
		if (multiplier > 1)
		{
			appendPerk(html, label, "x" + formatRate(base * multiplier), "x" + formatRate(base));
		}
	}

	private static void appendPerk(StringBuilder html, String label, String premium, String normal)
	{
		html.append("<tr><td>" + label + ": <font color=\"LEVEL\">" + premium + "</font> <font color=\"909090\">(normal " + normal + ")</font><br1></td></tr>");
	}

	/**
	 * @param rates the by-item-id rate map to read
	 * @param fallback the generic rate, used when adena carries no entry of its own
	 * @return the rate adena is dropped at, which NpcTemplate takes from the by-id map when present
	 */
	private static float rateForAdena(Map<Integer, Float> rates, float fallback)
	{
		final Float rate = rates.get(ADENA_ID);
		return rate == null ? fallback : rate.floatValue();
	}

	/** Keeps whole multipliers off the decimal point, so x2 does not read as x2.0. */
	private static String formatRate(float value)
	{
		return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
	}

	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}
}
