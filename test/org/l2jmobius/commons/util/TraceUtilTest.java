package org.l2jmobius.commons.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class TraceUtilTest {

    @Test
    public void testGetStackTrace() {
        String message = "Test exception";
        Exception ex = new Exception(message);
        String stackTrace = TraceUtil.getStackTrace(ex);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains(message));
        assertTrue(stackTrace.contains(ex.getClass().getName()));
        assertTrue(stackTrace.contains("testGetStackTrace"));
    }

    @Test
    public void testGetTraceString() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String traceString = TraceUtil.getTraceString(elements);

        assertNotNull(traceString);
        for (StackTraceElement element : elements) {
            assertTrue(traceString.contains(element.toString()));
        }

        if (elements.length > 1) {
            assertTrue(traceString.contains(System.lineSeparator()));
        }
    }

    @Test
    public void testGetTraceStringEmpty() {
        StackTraceElement[] elements = new StackTraceElement[0];
        String traceString = TraceUtil.getTraceString(elements);

        assertEquals("", traceString);
    }
}
