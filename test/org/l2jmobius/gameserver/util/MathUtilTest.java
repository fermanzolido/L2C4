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
}
