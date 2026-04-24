package org.l2jmobius.commons.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.Duration;

import org.junit.Test;

public class TimeUtilTest
{
	@Test
	public void testParseDurationMilliseconds()
	{
		assertEquals(Duration.ofMillis(5000), TimeUtil.parseDuration("5000"));
		assertEquals(Duration.ofMillis(500), TimeUtil.parseDuration("500ms"));
	}

	@Test
	public void testParseDurationSeconds()
	{
		assertEquals(Duration.ofSeconds(10), TimeUtil.parseDuration("10s"));
		assertEquals(Duration.ofSeconds(10), TimeUtil.parseDuration("10sec"));
		assertEquals(Duration.ofSeconds(10), TimeUtil.parseDuration("10secs"));
		assertEquals(Duration.ofSeconds(10), TimeUtil.parseDuration("10SEC"));
	}

	@Test
	public void testParseDurationMinutes()
	{
		assertEquals(Duration.ofMinutes(5), TimeUtil.parseDuration("5m"));
		assertEquals(Duration.ofMinutes(5), TimeUtil.parseDuration("5min"));
		assertEquals(Duration.ofMinutes(5), TimeUtil.parseDuration("5mins"));
	}

	@Test
	public void testParseDurationHours()
	{
		assertEquals(Duration.ofHours(2), TimeUtil.parseDuration("2h"));
		assertEquals(Duration.ofHours(2), TimeUtil.parseDuration("2hour"));
		assertEquals(Duration.ofHours(2), TimeUtil.parseDuration("2hours"));
	}

	@Test
	public void testParseDurationDays()
	{
		assertEquals(Duration.ofDays(1), TimeUtil.parseDuration("1d"));
		assertEquals(Duration.ofDays(1), TimeUtil.parseDuration("1day"));
		assertEquals(Duration.ofDays(1), TimeUtil.parseDuration("1days"));
	}

	@Test
	public void testParseDurationWeeks()
	{
		assertEquals(Duration.ofDays(7), TimeUtil.parseDuration("1w"));
		assertEquals(Duration.ofDays(7), TimeUtil.parseDuration("1week"));
		assertEquals(Duration.ofDays(14), TimeUtil.parseDuration("2weeks"));
	}

	@Test
	public void testParseDurationMonths()
	{
		assertEquals(Duration.ofDays(30), TimeUtil.parseDuration("1month"));
		assertEquals(Duration.ofDays(60), TimeUtil.parseDuration("2months"));
	}

	@Test
	public void testParseDurationYears()
	{
		assertEquals(Duration.ofDays(365), TimeUtil.parseDuration("1y"));
		assertEquals(Duration.ofDays(365), TimeUtil.parseDuration("1year"));
		assertEquals(Duration.ofDays(730), TimeUtil.parseDuration("2years"));
	}

	@Test
	public void testParseDurationLargeValue()
	{
		// Test with a value larger than Integer.MAX_VALUE
		long largeValue = (long) Integer.MAX_VALUE + 1;
		assertEquals(Duration.ofMillis(largeValue), TimeUtil.parseDuration(String.valueOf(largeValue)));
	}

	@Test
	public void testParseDurationInvalidFormat()
	{
		assertThrows(IllegalArgumentException.class, () -> TimeUtil.parseDuration(""));
		assertThrows(IllegalArgumentException.class, () -> TimeUtil.parseDuration("no-digits"));
		assertThrows(IllegalArgumentException.class, () -> TimeUtil.parseDuration("10unknown"));
	}

	@Test
	public void testFormatDuration()
	{
		assertEquals("0 milliseconds", TimeUtil.formatDuration(0));
		assertEquals("1 millisecond", TimeUtil.formatDuration(1));
		assertEquals("1 second", TimeUtil.formatDuration(1000));
		assertEquals("1 second, 500 milliseconds", TimeUtil.formatDuration(1500));
		assertEquals("1 minute, 1 second", TimeUtil.formatDuration(61000));
		assertEquals("1 hour, 1 minute", TimeUtil.formatDuration(3660000));
		assertEquals("1 day, 1 hour", TimeUtil.formatDuration(90000000));
		assertEquals("2 days, 2 hours, 2 minutes, 2 seconds, 2 milliseconds", TimeUtil.formatDuration(
			(2 * 24 * 60 * 60 * 1000L) + (2 * 60 * 60 * 1000L) + (2 * 60 * 1000L) + (2 * 1000L) + 2));
	}
}
