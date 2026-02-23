package handlers.bypasshandlers;

import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * Menu bypass handler.
 * 
 * @author Antigravity
 */
public class MenuBypass implements IBypassHandler {
    private static final String[] COMMANDS = {
            "voiced_menu",
            "voiced_menu_exp",
            "voiced_menu_trade",
            "voiced_menu_silence",
            "voiced_menu_henna"
    };

    @Override
    public boolean onCommand(String command, Player player, Creature target) {
        if (command.equals("voiced_menu")) {
            final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("menu");
            if (vch != null) {
                vch.onCommand("menu", player, null);
            }
        } else if (command.equals("voiced_menu_exp")) {
            final boolean expOff = player.getVariables().getBoolean("EXPOFF", false);
            if (expOff) {
                player.enableExpGain();
                player.getVariables().set("EXPOFF", false);
                player.sendMessage("Ganancia de experiencia habilitada.");
            } else {
                player.disableExpGain();
                player.getVariables().set("EXPOFF", true);
                player.sendMessage("Ganancia de experiencia deshabilitada.");
            }
            refreshMenu(player);
        } else if (command.equals("voiced_menu_trade")) {
            player.setTradeRefusal(!player.getTradeRefusal());
            player.sendMessage("Rechazo de trade: " + (player.getTradeRefusal() ? "ACTIVADO" : "DESACTIVADO"));
            refreshMenu(player);
        } else if (command.equals("voiced_menu_silence")) {
            player.setSilenceMode(!player.isSilenceMode());
            player.sendMessage("Modo silencio: " + (player.isSilenceMode() ? "ACTIVADO" : "DESACTIVADO"));
            refreshMenu(player);
        } else if (command.equals("voiced_menu_henna")) {
            final boolean hennaPenalty = player.getVariables().getBoolean("HENNA_PENALTY", true);
            player.getVariables().set("HENNA_PENALTY", !hennaPenalty);
            player.sendMessage("Penalizacion de tintes: " + (!hennaPenalty ? "ACTIVADA" : "DESACTIVADA"));
            refreshMenu(player);
        }

        return true;
    }

    private void refreshMenu(Player player) {
        final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("menu");
        if (vch != null) {
            vch.onCommand("menu", player, null);
        }
    }

    @Override
    public String[] getCommandList() {
        return COMMANDS;
    }
}
