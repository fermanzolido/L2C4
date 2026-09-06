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
package org.l2jmobius.commons.enums;

/**
 * The outcome of a password change, decided by the login server and reported to the player by the
 * game server.
 * <p>
 * It lives in commons because it crosses between the two servers: only the login server can judge
 * a password, and only the game server can tell the player. Both jars are built from the same
 * classes here, so there is one definition rather than two that can drift apart.
 * <p>
 * The ordinal travels on the wire. Append new values at the end; reordering them silently changes
 * what an older server on the other end of the connection understands.
 * @author Mobius
 */
public enum PasswordChangeResult
{
	/** The password was changed and the new one is stored. */
	SUCCESS,
	/** The current password given does not match the stored one. */
	WRONG_CURRENT_PASSWORD,
	/** No account by that name, which should not happen for a player who is logged in. */
	ACCOUNT_NOT_FOUND,
	/** The login server could not read or write the account. */
	DATABASE_ERROR
}
