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
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilTest
{
	@Test
	public void testAppendObjects()
	{
		StringBuilder sb = new StringBuilder();
		StringUtil.append(sb, "Hello ", 123, " World ", true);
		assertEquals("Hello 123 World true", sb.toString());
	}

	@Test
	public void testConcatObjects()
	{
		String result = StringUtil.concat("Hello ", 456, " World ", false);
		assertEquals("Hello 456 World false", result);
	}

	@Test
	public void testAppendStrings()
	{
		StringBuilder sb = new StringBuilder();
		StringUtil.append(sb, "A", "B", "C");
		assertEquals("ABC", sb.toString());
	}

	@Test
	public void testConcatStrings()
	{
		String result = StringUtil.concat("X", "Y", "Z");
		assertEquals("XYZ", result);
	}

	@Test
	public void testIsAlphaNumeric()
	{
		assertTrue(StringUtil.isAlphaNumeric("abc123"));
		assertFalse(StringUtil.isAlphaNumeric("abc 123"));
		assertFalse(StringUtil.isAlphaNumeric(null));
		assertFalse(StringUtil.isAlphaNumeric(""));
	}

	@Test
	public void testIsNumeric()
	{
		assertTrue(StringUtil.isNumeric("123456"));
		assertFalse(StringUtil.isNumeric("123a456"));
		assertFalse(StringUtil.isNumeric(null));
		assertFalse(StringUtil.isNumeric(""));
	}

	@Test
	public void testIsInteger()
	{
		assertTrue(StringUtil.isInteger("123"));
		assertTrue(StringUtil.isInteger("-123"));
		assertFalse(StringUtil.isInteger("12.3"));
		assertFalse(StringUtil.isInteger("abc"));
		assertFalse(StringUtil.isInteger(null));
		assertFalse(StringUtil.isInteger(""));
	}
}
