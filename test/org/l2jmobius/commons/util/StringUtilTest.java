package org.l2jmobius.commons.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilTest
{
	@Test
	public void testIsInteger()
	{
		assertFalse("Null should not be an integer", StringUtil.isInteger(null));
		assertFalse("Empty string should not be an integer", StringUtil.isInteger(""));
		assertTrue("Positive integer should be an integer", StringUtil.isInteger("123"));
		assertTrue("Negative integer should be an integer", StringUtil.isInteger("-123"));
		assertTrue("Positive integer with + sign should be an integer", StringUtil.isInteger("+123"));
		assertFalse("Non-numeric string should not be an integer", StringUtil.isInteger("abc"));
		assertFalse("Floating point string should not be an integer", StringUtil.isInteger("123.45"));
		assertFalse("String with spaces should not be an integer", StringUtil.isInteger(" 123 "));
		assertFalse("Integer overflow should return false", StringUtil.isInteger("2147483648")); // Integer.MAX_VALUE + 1
		assertFalse("Integer underflow should return false", StringUtil.isInteger("-2147483649")); // Integer.MIN_VALUE - 1
	}

	@Test
	public void testIsNumeric()
	{
		assertFalse("Null should not be numeric", StringUtil.isNumeric(null));
		assertFalse("Empty string should not be numeric", StringUtil.isNumeric(""));
		assertTrue("Digits only should be numeric", StringUtil.isNumeric("123"));
		assertFalse("Negative sign should not be numeric in isNumeric", StringUtil.isNumeric("-123"));
		assertFalse("Alphanumeric should not be numeric", StringUtil.isNumeric("123a"));
	}

	@Test
	public void testIsAlphaNumeric()
	{
		assertFalse("Null should not be alphanumeric", StringUtil.isAlphaNumeric(null));
		assertFalse("Empty string should not be alphanumeric", StringUtil.isAlphaNumeric(""));
		assertTrue("Letters and digits should be alphanumeric", StringUtil.isAlphaNumeric("abc123"));
		assertFalse("Spaces should not be alphanumeric", StringUtil.isAlphaNumeric("abc 123"));
		assertFalse("Special characters should not be alphanumeric", StringUtil.isAlphaNumeric("abc!@#"));
	}

	@Test
	public void testIsFloat()
	{
		assertFalse("Null should not be a float", StringUtil.isFloat(null));
		assertFalse("Empty string should not be a float", StringUtil.isFloat(""));
		assertTrue("Valid float should be a float", StringUtil.isFloat("123.45"));
		assertTrue("Integer should be a float", StringUtil.isFloat("123"));
		assertFalse("Invalid string should not be a float", StringUtil.isFloat("abc"));
	}

	@Test
	public void testIsDouble()
	{
		assertFalse("Null should not be a double", StringUtil.isDouble(null));
		assertFalse("Empty string should not be a double", StringUtil.isDouble(""));
		assertTrue("Valid double should be a double", StringUtil.isDouble("123.45"));
		assertTrue("Integer should be a double", StringUtil.isDouble("123"));
		assertFalse("Invalid string should not be a double", StringUtil.isDouble("abc"));
	}
}
