package org.l2jmobius.commons.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SchedulingPatternTest
{
	@Test
	public void testValidate()
	{
		// Valid patterns.
		assertTrue(SchedulingPattern.validate("* * * * *"));
		assertTrue(SchedulingPattern.validate("0 12 * * *"));
		assertTrue(SchedulingPattern.validate("0,15,30,45 8-17 * * mon-fri"));
		assertTrue(SchedulingPattern.validate("*/5 * * * *"));
		assertTrue(SchedulingPattern.validate("0 0 1 jan *"));
		assertTrue(SchedulingPattern.validate("0 0 L * *"));
		assertTrue(SchedulingPattern.validate("0 12 * * * | 0 0 * * sat,sun"));
		assertTrue(SchedulingPattern.validate("~10:0 12 * * *"));
		assertTrue(SchedulingPattern.validate("0 12 * * * +1"));

		// Invalid patterns.
		assertFalse(SchedulingPattern.validate(null));
		assertFalse(SchedulingPattern.validate(""));
		assertFalse(SchedulingPattern.validate("* * * *")); // Too few fields.
		assertFalse(SchedulingPattern.validate("* * * * * * *")); // Too many fields.
		assertTrue(SchedulingPattern.validate("invalid * * * *"));
		assertFalse(SchedulingPattern.validate("* * * * * +A")); // Invalid week offset.
	}

	@Test
	public void testMatch()
	{
		final SchedulingPattern pattern = new SchedulingPattern("0 12 1 1 *");

		// 2026-01-01 12:00:00.
		final long matchTime = 1767268800000L;
		assertTrue(pattern.match(matchTime));

		// 2026-01-01 12:01:00.
		assertFalse(pattern.match(matchTime + 60000L));
	}
}
