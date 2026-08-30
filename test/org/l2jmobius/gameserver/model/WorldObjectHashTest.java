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
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.l2jmobius.gameserver.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * {@link WorldObject} compares by object id, so equal objects have to answer equal
 * hashes. Around a hundred sets and maps in the server hold world objects directly, and
 * with the inherited identity hash a lookup by an equal but distinct instance landed in
 * the wrong bucket and missed.
 */
public class WorldObjectHashTest
{
	/** The smallest thing that is a WorldObject: the class is abstract only for sendInfo. */
	private static class TestObject extends WorldObject
	{
		TestObject(int objectId)
		{
			super(objectId);
		}

		@Override
		public void sendInfo(Player player)
		{
		}

		@Override
		public boolean isAutoAttackable(Creature attacker)
		{
			return false;
		}
	}

	@Test
	public void equalObjectsAnswerEqualHashes()
	{
		final WorldObject one = new TestObject(4242);
		final WorldObject other = new TestObject(4242);

		assertTrue("two objects with the same id are equal", one.equals(other));
		assertEquals("so they have to hash the same", one.hashCode(), other.hashCode());
	}

	@Test
	public void differentIdsAreNotEqual()
	{
		final WorldObject one = new TestObject(1);
		final WorldObject other = new TestObject(2);

		assertTrue("different ids are different objects", !one.equals(other));
	}

	@Test
	public void aSetFindsAnObjectByAnEqualInstance()
	{
		final Set<WorldObject> set = new HashSet<>();
		set.add(new TestObject(7));

		// This is the case that failed: the same id, a different instance.
		assertTrue("the set has to recognise an equal instance", set.contains(new TestObject(7)));
		assertTrue("and removing by an equal instance has to work", set.remove(new TestObject(7)));
		assertTrue("leaving it empty", set.isEmpty());
	}

	@Test
	public void aMapKeyedByObjectResolvesByAnEqualInstance()
	{
		final Map<WorldObject, String> map = new HashMap<>();
		map.put(new TestObject(9), "value");

		assertNotNull("the map has to resolve an equal key", map.get(new TestObject(9)));
		assertEquals("value", map.get(new TestObject(9)));
	}

	@Test
	public void addingTheSameIdTwiceDoesNotGrowTheSet()
	{
		final Set<WorldObject> set = new HashSet<>();
		set.add(new TestObject(11));
		set.add(new TestObject(11));

		assertEquals("one id is one element", 1, set.size());
	}
}
