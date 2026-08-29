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

import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;

/**
 * @author DrHouse
 */
public class DropProtection implements Runnable
{
	private volatile boolean _isProtected = false;
	// Its companion above is volatile because getOwner and isProtected are read with no
	// lock while run, unprotect and protect all write under one; this field was left out.
	private volatile Creature _owner = null;
	private ScheduledFuture<?> _task = null;
	
	private static final long PROTECTED_MILLIS_TIME = 15000;
	
	@Override
	public synchronized void run()
	{
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public boolean isProtected()
	{
		return _isProtected;
	}
	
	public Creature getOwner()
	{
		return _owner;
	}
	
	public synchronized boolean tryPickUp(Player actor)
	{
		return !_isProtected || (_owner == actor) || ((_owner.getParty() != null) && (_owner.getParty() == actor.getParty()));
	}
	
	public boolean tryPickUp(Pet pet)
	{
		return tryPickUp(pet.getOwner());
	}
	
	public synchronized void unprotect()
	{
		if (_task != null)
		{
			_task.cancel(false);
		}
		
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public synchronized void protect(Creature creature)
	{
		// This test used to sit after the two writes it exists to prevent, so the throw
		// left the item protected with no owner and no task to clear it -- which is the
		// one state that makes every later tryPickUp throw, for everyone, forever.
		if (creature == null)
		{
			throw new NullPointerException("Trying to protect dropped item to null owner");
		}
		
		unprotect();
		
		_isProtected = true;
		_owner = creature;
		_task = ThreadPool.schedule(this, PROTECTED_MILLIS_TIME);
	}
}
