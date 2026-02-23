package handlers.voicedcommandhandlers;

import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Menu voiced command handler.
 * 
 * @author Antigravity
 */
public class Menu implements IVoicedCommandHandler {
    private static final String[] VOICED_COMMANDS = {
            "menu"
    };

    @Override
    public boolean onCommand(String command, Player player, String params) {
        if (command.equals("menu")) {
            showMenu(player);
        }
        return true;
    }

    private void showMenu(Player player) {
        final NpcHtmlMessage html = new NpcHtmlMessage();
        html.setFile(player, "data/html/voiced/menu.htm");
        html.replace("%name%", player.getName());
        html.replace("%level%", String.valueOf(player.getLevel()));
        html.replace("%class%", player.getPlayerClass().toString());

        // EXP Gain
        final boolean expOff = player.getVariables().getBoolean("EXPOFF", false);
        html.replace("%exp_gain%", expOff ? "<a action=\"bypass -h voiced_menu_exp\">[Desactivado]</a>"
                : "<a action=\"bypass -h voiced_menu_exp\"><font color=\"00FF00\">[Activado]</font></a>");

        // Trade Refusal
        final boolean tradeRefusal = player.getTradeRefusal();
        html.replace("%trade_refusal%",
                tradeRefusal
                        ? "<a action=\"bypass -h voiced_menu_trade\"><font color=\"FF0000\">[Rechazando]</font></a>"
                        : "<a action=\"bypass -h voiced_menu_trade\">[Aceptando]</a>");

        // Silence Mode
        final boolean silence = player.isSilenceMode();
        html.replace("%silence%",
                silence ? "<a action=\"bypass -h voiced_menu_silence\"><font color=\"FF0000\">[Activado]</font></a>"
                        : "<a action=\"bypass -h voiced_menu_silence\">[Desactivado]</a>");

        // Henna Penalty
        final boolean hennaPenalty = player.getVariables().getBoolean("HENNA_PENALTY", true);
        html.replace("%henna_penalty%", hennaPenalty
                ? "<a action=\"bypass -h voiced_menu_henna\"><font color=\"FF0000\">[Con Penalizacion]</font></a>"
                : "<a action=\"bypass -h voiced_menu_henna\"><font color=\"00FF00\">[Sin Penalizacion]</font></a>");

        player.sendPacket(html);
    }

    @Override
    public String[] getCommandList() {
        return VOICED_COMMANDS;
    }
}
