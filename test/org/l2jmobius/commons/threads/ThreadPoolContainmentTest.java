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
package org.l2jmobius.commons.threads;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A scheduled task that throws is cancelled by the executor and never runs again. The
 * server holds 69 recurring tasks and every one of them is routed through the wrapper
 * that contains the throwable, so this property is what keeps them alive.
 */
public class ThreadPoolContainmentTest
{
	private Level _previousLevel;

	@Before
	public void quietenTheExpectedWarnings()
	{
		// The tasks below throw on purpose; their stack traces are not test failures.
		final Logger logger = Logger.getLogger(ThreadPool.class.getName());
		_previousLevel = logger.getLevel();
		logger.setLevel(Level.OFF);
		ThreadPool.init();
	}

	@After
	public void releaseThePools()
	{
		ThreadPool.shutdown();
		Logger.getLogger(ThreadPool.class.getName()).setLevel(_previousLevel);
	}

	@Test
	public void aRecurringTaskSurvivesItsOwnException() throws Exception
	{
		final AtomicInteger runs = new AtomicInteger();
		final CountDownLatch reachedThird = new CountDownLatch(3);

		final ScheduledFuture<?> task = ThreadPool.scheduleAtFixedRate(() ->
		{
			runs.incrementAndGet();
			reachedThird.countDown();
			throw new IllegalStateException("thrown on purpose");
		}, 10, 10);

		assertTrue("a recurring task that throws has to run again",
				reachedThird.await(10, TimeUnit.SECONDS));
		task.cancel(false);
		assertTrue("it ran at least three times, so it was never cancelled", runs.get() >= 3);
	}

	@Test
	public void aOneShotTaskThatThrowsDoesNotEscape() throws Exception
	{
		final CountDownLatch ran = new CountDownLatch(1);

		ThreadPool.execute(() ->
		{
			ran.countDown();
			throw new IllegalStateException("thrown on purpose");
		});

		assertTrue("the task ran", ran.await(10, TimeUnit.SECONDS));

		// The pool has to still accept work after one of its tasks threw.
		final CountDownLatch second = new CountDownLatch(1);
		ThreadPool.execute(second::countDown);
		assertTrue("the pool still takes work afterwards", second.await(10, TimeUnit.SECONDS));
	}
}
