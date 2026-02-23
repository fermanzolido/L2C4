/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model;

/**
 * @author Rayan RPG, JIV
 * @since 927
 */
public class NpcWalkerNode extends Location
{
	private final String _chatString;
	private final int _delay;
	private final boolean _runToLocation;
	
	public NpcWalkerNode(int moveX, int moveY, int moveZ, int delay, boolean runToLocation, String chatText)
	{
		super(moveX, moveY, moveZ);
		_delay = delay;
		_runToLocation = runToLocation;
		_chatString = (chatText == null) ? "" : chatText;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public boolean runToLocation()
	{
		return _runToLocation;
	}
	
	public String getChatText()
	{
		return _chatString;
	}
}
