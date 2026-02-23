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
package org.l2jmobius.gameserver.handler;

import java.util.EnumMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.enums.creature.InstanceType;

public class ActionHandler implements IHandler<IActionHandler, InstanceType>
{
	private final Map<InstanceType, IActionHandler> _actions;
	
	protected ActionHandler()
	{
		_actions = new EnumMap<>(InstanceType.class);
	}
	
	@Override
	public void registerHandler(IActionHandler handler)
	{
		_actions.put(handler.getInstanceType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IActionHandler handler)
	{
		_actions.remove(handler.getInstanceType());
	}
	
	@Override
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actions.get(t);
			if (result != null)
			{
				break;
			}
		}
		
		return result;
	}
	
	@Override
	public int size()
	{
		return _actions.size();
	}
	
	public static ActionHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ActionHandler INSTANCE = new ActionHandler();
	}
}
