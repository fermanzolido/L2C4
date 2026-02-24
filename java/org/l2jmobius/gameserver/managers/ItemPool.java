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
package org.l2jmobius.gameserver.managers;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.l2jmobius.gameserver.model.item.instance.Item;

/**
 * @author Mobius
 */
public class ItemPool
{
	private static final ItemPool INSTANCE = new ItemPool();
	private final Map<Integer, Queue<Item>> _pool = new ConcurrentHashMap<>();
	private final Map<Integer, AtomicInteger> _poolSizes = new ConcurrentHashMap<>();
	private static final int MAX_POOL_SIZE_PER_ID = 500;

	protected ItemPool()
	{
	}

	public static ItemPool getInstance()
	{
		return INSTANCE;
	}

	public boolean addItem(Item item)
	{
		if (item == null)
		{
			return false;
		}

		final int itemId = item.getId();
		final AtomicInteger size = _poolSizes.computeIfAbsent(itemId, k -> new AtomicInteger(0));
		if (size.get() < MAX_POOL_SIZE_PER_ID)
		{
			_pool.computeIfAbsent(itemId, k -> new ConcurrentLinkedQueue<>()).offer(item);
			size.incrementAndGet();
			return true;
		}
		return false;
	}

	public Item getItem(int itemId)
	{
		final Queue<Item> queue = _pool.get(itemId);
		if (queue != null)
		{
			final Item item = queue.poll();
			if (item != null)
			{
				_poolSizes.get(itemId).decrementAndGet();
				return item;
			}
		}
		return null;
	}
}
