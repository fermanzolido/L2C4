package org.l2jmobius.gameserver.model.zone.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * {@code EffectZone.getSkillLevel} used to ask its skill map three times for one question --
 * a null test on the field, a containsKey, and then a get whose Integer was unboxed straight
 * into the int it returns. {@code clearSkills} empties that map and the field itself is
 * replaced at runtime, so an entry could vanish between the test and the read and take the
 * caller down with it.
 * @author Claude
 */
public class EffectZoneTest
{
	@Test
	public void aZoneWithNoSkillsAnswersZero()
	{
		final EffectZone zone = new EffectZone(1);
		assertEquals("A zone that was never given skills has no level for any of them", 0, zone.getSkillLevel(1234));
	}

	@Test
	public void aSkillTheZoneDoesNotCarryAnswersZero()
	{
		final EffectZone zone = new EffectZone(2);
		zone.setParameter("skillIdLvl", "1234-3");

		assertEquals("The declared skill keeps its level", 3, zone.getSkillLevel(1234));
		assertEquals("A skill this zone never declared must answer zero, not throw", 0, zone.getSkillLevel(5678));
	}

	@Test
	public void clearingTheSkillsLeavesEveryLookupAtZero()
	{
		final EffectZone zone = new EffectZone(3);
		zone.setParameter("skillIdLvl", "1234-3");
		zone.clearSkills();

		assertEquals("A cleared zone must answer zero rather than unbox a missing entry", 0, zone.getSkillLevel(1234));
	}

	/**
	 * The three cases above pass against the old three-lookup version too, because a single
	 * thread never sees an entry disappear between the containsKey and the get. This one does
	 * not: it clears the map underneath a reader, which is exactly what the old code unboxed
	 * a null out of.
	 */
	@Test
	public void aReaderSurvivesTheSkillsBeingClearedUnderneathIt() throws Exception
	{
		final EffectZone zone = new EffectZone(4);
		final AtomicReference<Throwable> thrown = new AtomicReference<>();
		final CountDownLatch done = new CountDownLatch(2);
		final long deadline = System.currentTimeMillis() + 2000;

		final Thread writer = new Thread(() ->
		{
			try
			{
				while (System.currentTimeMillis() < deadline)
				{
					zone.setParameter("skillIdLvl", "1234-3");
					zone.clearSkills();
				}
			}
			catch (Throwable t)
			{
				thrown.compareAndSet(null, t);
			}
			finally
			{
				done.countDown();
			}
		});

		final Thread reader = new Thread(() ->
		{
			try
			{
				while (System.currentTimeMillis() < deadline)
				{
					final int level = zone.getSkillLevel(1234);
					assertTrue("A level is either the declared one or nothing at all", (level == 0) || (level == 3));
				}
			}
			catch (Throwable t)
			{
				thrown.compareAndSet(null, t);
			}
			finally
			{
				done.countDown();
			}
		});

		writer.setDaemon(true);
		reader.setDaemon(true);
		writer.start();
		reader.start();
		done.await(30, TimeUnit.SECONDS);

		final Throwable failure = thrown.get();
		if (failure != null)
		{
			throw new AssertionError("Reading a skill level while the map is cleared must not throw", failure);
		}
	}
}
