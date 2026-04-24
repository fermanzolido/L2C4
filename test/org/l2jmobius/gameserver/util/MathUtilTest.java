package org.l2jmobius.gameserver.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilTest {

    @Test
    public void testClampInt() {
        // Value within range
        assertEquals(5, MathUtil.clamp(5, 1, 10));
        // Value below min
        assertEquals(1, MathUtil.clamp(0, 1, 10));
        // Value above max
        assertEquals(10, MathUtil.clamp(11, 1, 10));
        // Value at min
        assertEquals(1, MathUtil.clamp(1, 1, 10));
        // Value at max
        assertEquals(10, MathUtil.clamp(10, 1, 10));
        // Min == Max
        assertEquals(10, MathUtil.clamp(5, 10, 10));
        assertEquals(10, MathUtil.clamp(15, 10, 10));
        // Negative values
        assertEquals(-5, MathUtil.clamp(-5, -10, -1));
        assertEquals(-10, MathUtil.clamp(-15, -10, -1));
        assertEquals(-1, MathUtil.clamp(0, -10, -1));
        // Extreme values
        assertEquals(100, MathUtil.clamp(Integer.MAX_VALUE, 0, 100));
        assertEquals(0, MathUtil.clamp(Integer.MIN_VALUE, 0, 100));
    }

    @Test
    public void testClampLong() {
        // Value within range
        assertEquals(5L, MathUtil.clamp(5L, 1L, 10L));
        // Value below min
        assertEquals(1L, MathUtil.clamp(0L, 1L, 10L));
        // Value above max
        assertEquals(10L, MathUtil.clamp(11L, 1L, 10L));
        // Value at min
        assertEquals(1L, MathUtil.clamp(1L, 1L, 10L));
        // Value at max
        assertEquals(10L, MathUtil.clamp(10L, 1L, 10L));
        // Min == Max
        assertEquals(10L, MathUtil.clamp(5L, 10L, 10L));
        // Negative values
        assertEquals(-5L, MathUtil.clamp(-5L, -10L, -1L));
        // Extreme values
        assertEquals(100L, MathUtil.clamp(Long.MAX_VALUE, 0L, 100L));
        assertEquals(0L, MathUtil.clamp(Long.MIN_VALUE, 0L, 100L));
    }

    @Test
    public void testClampDouble() {
        // Value within range
        assertEquals(5.5, MathUtil.clamp(5.5, 1.0, 10.0), 0.0);
        // Value below min
        assertEquals(1.0, MathUtil.clamp(0.5, 1.0, 10.0), 0.0);
        // Value above max
        assertEquals(10.0, MathUtil.clamp(10.5, 1.0, 10.0), 0.0);
        // Value at min
        assertEquals(1.0, MathUtil.clamp(1.0, 1.0, 10.0), 0.0);
        // Value at max
        assertEquals(10.0, MathUtil.clamp(10.0, 1.0, 10.0), 0.0);
        // Min == Max
        assertEquals(10.0, MathUtil.clamp(5.0, 10.0, 10.0), 0.0);
        // Negative values
        assertEquals(-5.5, MathUtil.clamp(-5.5, -10.0, -1.0), 0.0);
        // Extreme values
        assertEquals(100.0, MathUtil.clamp(Double.MAX_VALUE, 0.0, 100.0), 0.0);
        assertEquals(0.0, MathUtil.clamp(-Double.MAX_VALUE, 0.0, 100.0), 0.0);
    }

    @Test
    public void testGetIndexOfMinValue() {
        assertEquals(0, MathUtil.getIndexOfMinValue(1, 2, 3));
        assertEquals(2, MathUtil.getIndexOfMinValue(3, 2, 1));
        assertEquals(1, MathUtil.getIndexOfMinValue(5, 2, 8, 4));
        assertEquals(0, MathUtil.getIndexOfMinValue(10));
        assertEquals(1, MathUtil.getIndexOfMinValue(10, -5, 20));
        assertEquals(0, MathUtil.getIndexOfMinValue(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testGetIndexOfMaxValue() {
        assertEquals(2, MathUtil.getIndexOfMaxValue(1, 2, 3));
        assertEquals(0, MathUtil.getIndexOfMaxValue(3, 2, 1));
        assertEquals(2, MathUtil.getIndexOfMaxValue(5, 2, 8, 4));
        assertEquals(0, MathUtil.getIndexOfMaxValue(10));
        assertEquals(2, MathUtil.getIndexOfMaxValue(10, -5, 20));
        assertEquals(2, MathUtil.getIndexOfMaxValue(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testMinInt() {
        assertEquals(1, MathUtil.min(1, 2, 3));
        assertEquals(1, MathUtil.min(3, 2, 1));
        assertEquals(2, MathUtil.min(5, 2, 8, 4));
        assertEquals(10, MathUtil.min(10));
        assertEquals(-5, MathUtil.min(10, -5, 20));
        assertEquals(Integer.MIN_VALUE, MathUtil.min(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, MathUtil.max(1, 2, 3));
        assertEquals(3, MathUtil.max(3, 2, 1));
        assertEquals(8, MathUtil.max(5, 2, 8, 4));
        assertEquals(10, MathUtil.max(10));
        assertEquals(20, MathUtil.max(10, -5, 20));
        assertEquals(Integer.MAX_VALUE, MathUtil.max(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test
    public void testMinLong() {
        assertEquals(1L, MathUtil.min(1L, 2L, 3L));
        assertEquals(1L, MathUtil.min(3L, 2L, 1L));
        assertEquals(2L, MathUtil.min(5L, 2L, 8L, 4L));
        assertEquals(10L, MathUtil.min(10L));
        assertEquals(-5L, MathUtil.min(10L, -5L, 20L));
        assertEquals(Long.MIN_VALUE, MathUtil.min(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test
    public void testMaxLong() {
        assertEquals(3L, MathUtil.max(1L, 2L, 3L));
        assertEquals(3L, MathUtil.max(3L, 2L, 1L));
        assertEquals(8L, MathUtil.max(5L, 2L, 8L, 4L));
        assertEquals(10L, MathUtil.max(10L));
        assertEquals(20L, MathUtil.max(10L, -5L, 20L));
        assertEquals(Long.MAX_VALUE, MathUtil.max(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.1, MathUtil.min(1.1, 2.2, 3.3), 0.0);
        assertEquals(1.1, MathUtil.min(3.3, 2.2, 1.1), 0.0);
        assertEquals(2.2, MathUtil.min(5.5, 2.2, 8.8, 4.4), 0.0);
        assertEquals(10.0, MathUtil.min(10.0), 0.0);
        assertEquals(-5.5, MathUtil.min(10.0, -5.5, 20.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtil.min(Double.NEGATIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.3, MathUtil.max(1.1, 2.2, 3.3), 0.0);
        assertEquals(3.3, MathUtil.max(3.3, 2.2, 1.1), 0.0);
        assertEquals(8.8, MathUtil.max(5.5, 2.2, 8.8, 4.4), 0.0);
        assertEquals(10.0, MathUtil.max(10.0), 0.0);
        assertEquals(20.0, MathUtil.max(10.0, -5.5, 20.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtil.max(Double.NEGATIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY), 0.0);
    }
}
