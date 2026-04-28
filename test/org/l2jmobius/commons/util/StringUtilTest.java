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
import org.junit.Test;

/**
 * Unit tests for {@link StringUtil}.
 * @author Mobius
 */
public class StringUtilTest
{
	@Test
	public void testAppendObject()
	{
		final StringBuilder sb = new StringBuilder("Initial");
		StringUtil.append(sb, " ", 123, " ", null, " ", 45.67);
		assertEquals("Initial 123 null 45.67", sb.toString());
	}

	@Test
	public void testConcatObject()
	{
		assertEquals("123 null 45.67", StringUtil.concat(123, " ", null, " ", 45.67));
	}

	@Test
	public void testAppendString()
	{
		final StringBuilder sb = new StringBuilder("Initial");
		StringUtil.append(sb, " ", "abc", " ", null, " ", "def");
		assertEquals("Initial abc null def", sb.toString());
	}

	@Test
	public void testConcatString()
	{
		assertEquals("abc null def", StringUtil.concat("abc", " ", null, " ", "def"));
	}
}
