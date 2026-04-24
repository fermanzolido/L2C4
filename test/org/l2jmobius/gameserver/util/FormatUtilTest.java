package org.l2jmobius.gameserver.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class FormatUtilTest {

    @Test
    public void testFormatAdena() {
        assertEquals("0", FormatUtil.formatAdena(0));
        assertEquals("1,000", FormatUtil.formatAdena(1000));
        assertEquals("1,234,567", FormatUtil.formatAdena(1234567));
        assertEquals("-1,000", FormatUtil.formatAdena(-1000));
        assertEquals("9,223,372,036,854,775,807", FormatUtil.formatAdena(Long.MAX_VALUE));
        assertEquals("-9,223,372,036,854,775,808", FormatUtil.formatAdena(Long.MIN_VALUE));
    }
}
