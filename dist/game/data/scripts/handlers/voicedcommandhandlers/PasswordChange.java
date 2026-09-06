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

import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.config.custom.PasswordChangeConfig;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * Lets a player change the password of the account they are logged into.
 * <p>
 * Nothing is decided here. Passwords live on the login server, so this only checks the shape of
 * what was typed and forwards it; whether the current password is right is the login server's
 * answer to give, and it comes back as a message to this character.
 * @author Mobius
 */
public class PasswordChange implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"changepassword"
	};

	@Override
	public boolean onCommand(String command, Player player, String params)
	{
		if (!PasswordChangeConfig.ALLOW_CHANGE_PASSWORD)
		{
			player.sendMessage("Changing your password is not allowed on this server.");
			return false;
		}

		if (params == null)
		{
			sendUsage(player);
			return false;
		}

		// Split on runs of whitespace: a password cannot contain a space, so anything else in the
		// line is a mistyped command rather than part of a password.
		final String[] parts = params.trim().split("\\s+");
		if (parts.length != 3)
		{
			sendUsage(player);
			return false;
		}

		final String currentPassword = parts[0];
		final String newPassword = parts[1];
		final String repeatedPassword = parts[2];

		if (!newPassword.equals(repeatedPassword))
		{
			player.sendMessage("The two new passwords do not match.");
			return false;
		}

		if (newPassword.length() < PasswordChangeConfig.MINIMUM_PASSWORD_LENGTH)
		{
			player.sendMessage("Your new password is too short.");
			return false;
		}

		// The client's own account name field stops at 14 characters, so a longer password could be
		// stored here and then never be typeable at the login screen.
		if (newPassword.length() > 16)
		{
			player.sendMessage("Your new password is too long.");
			return false;
		}

		if (newPassword.equals(currentPassword))
		{
			player.sendMessage("Your new password is the same as your current one.");
			return false;
		}

		LoginServerThread.getInstance().sendChangePassword(player.getAccountName(), player.getName(), currentPassword, newPassword);
		player.sendMessage("Your password change has been sent.");
		return true;
	}

	private static void sendUsage(Player player)
	{
		player.sendMessage("Usage: .changepassword <current password> <new password> <new password again>");
	}

	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}
}
