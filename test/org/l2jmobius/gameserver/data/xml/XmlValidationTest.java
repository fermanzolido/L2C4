package org.l2jmobius.gameserver.data.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class XmlValidationTest {
    private static final Pattern XML_FILE_PATTERN = Pattern.compile("\\d+\\.xml", Pattern.CASE_INSENSITIVE);
    private static final String REGEX = "\\d+\\.xml";

    @Test
    public void testRegexLogic() {
        // Valid cases
        assertTrue(isValid("123.xml"));
        assertTrue(isValid("1.xml"));
        assertTrue(isValid("123.XML"));
        assertTrue(isValid("123.Xml"));

        // Invalid cases
        assertFalse(isValid("abc.xml"));
        assertFalse(isValid("123.txt"));
        assertFalse(isValid(".xml"));
        assertFalse(isValid("123xml"));
        assertFalse(isValid("123.xml.bak"));
    }

    private boolean isValid(String filename) {
        return XML_FILE_PATTERN.matcher(filename).matches();
    }

    @Test
    public void benchmark() {
        int iterations = 1_000_000;
        String filename = "12345.xml";

        // Warmup
        for (int i = 0; i < 100_000; i++) {
            filename.toLowerCase().matches(REGEX);
            XML_FILE_PATTERN.matcher(filename).matches();
        }

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            filename.toLowerCase().matches(REGEX);
        }
        long end = System.nanoTime();
        long legacyTime = end - start;

        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            XML_FILE_PATTERN.matcher(filename).matches();
        }
        end = System.nanoTime();
        long optimizedTime = end - start;

        System.out.println("Benchmark results for " + iterations + " iterations:");
        System.out.println("Legacy (String.matches): " + (legacyTime / 1_000_000.0) + " ms");
        System.out.println("Optimized (Pattern): " + (optimizedTime / 1_000_000.0) + " ms");
        System.out.println("Improvement: " + ((legacyTime - optimizedTime) * 100.0 / legacyTime) + "%");
    }
}
