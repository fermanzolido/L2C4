package org.l2jmobius.gameserver.model;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

/**
 * A spawn's scheduled count is raised by the thread that saw an npc decay and lowered by the
 * respawn manager on its own thread, which used to reach into the field directly. The pair is
 * then read together to decide whether another respawn fits, so a single lost decrement
 * leaves that sum permanently at the maximum and the spawn never returns for the rest of the
 * server's uptime -- silently, and it accumulates.
 * <p>
 * A test cannot prove a race is gone, so this drives enough concurrent decrements that a lost
 * one is overwhelmingly likely to show, and asserts the counter lands exactly on zero.
 * <p>
 * {@code new Spawn(null)} returns before it reflects on a template class, so it costs nothing
 * to build; the counter is private, which is why this reads it back through reflection rather
 * than widening the class for a test.
 * @author Claude
 */
public class SpawnCounterTest
{
	private static final int THREADS = 8;
	private static final int STEPS_PER_THREAD = 2000;

	@Test
	public void everyScheduledRespawnIsCountedBackDown() throws Exception
	{
		final Spawn spawn = new Spawn((NpcTemplate) null);
		final int total = THREADS * STEPS_PER_THREAD;
		setScheduledCount(spawn, total);

		runConcurrently(spawn, STEPS_PER_THREAD);

		assertEquals("Every scheduled respawn must be counted back down", 0, scheduledCount(spawn));
	}

	@Test
	public void theCounterNeverFallsBelowZero() throws Exception
	{
		final Spawn spawn = new Spawn((NpcTemplate) null);

		// Half as many scheduled as there are calls: the rest must find nothing to take.
		setScheduledCount(spawn, (THREADS * STEPS_PER_THREAD) / 2);

		runConcurrently(spawn, STEPS_PER_THREAD);

		assertEquals("Taking from an empty counter must leave it at zero, not below", 0, scheduledCount(spawn));
	}

	private static void runConcurrently(Spawn spawn, int steps) throws Exception
	{
		final CountDownLatch go = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(THREADS);

		for (int i = 0; i < THREADS; i++)
		{
			final Thread thread = new Thread(() ->
			{
				try
				{
					go.await();
					for (int step = 0; step < steps; step++)
					{
						spawn.decreaseScheduledCount();
					}
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				finally
				{
					done.countDown();
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		go.countDown();
		done.await(60, TimeUnit.SECONDS);
	}

	private static Field scheduledCountField() throws Exception
	{
		final Field field = Spawn.class.getDeclaredField("_scheduledCount");
		field.setAccessible(true);
		return field;
	}

	private static void setScheduledCount(Spawn spawn, int value) throws Exception
	{
		scheduledCountField().setInt(spawn, value);
	}

	private static int scheduledCount(Spawn spawn) throws Exception
	{
		return scheduledCountField().getInt(spawn);
	}
}
