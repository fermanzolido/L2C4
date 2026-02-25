package org.l2jmobius.commons.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;

import org.junit.Test;

public class SubnetTest
{
	@Test
	public void testValidSubnet() throws Exception
	{
		// Test valid CIDR notation
		Subnet subnet = new Subnet("192.168.1.0/24");

		// Valid cases
		assertTrue("Address in subnet should be accepted",
				subnet.isInSubnet(java.net.InetAddress.getByName("192.168.1.5").getAddress()));
		assertTrue("Address in subnet should be accepted",
				subnet.isInSubnet(java.net.InetAddress.getByName("192.168.1.254").getAddress()));

		// Invalid cases
		assertFalse("Address outside subnet should be rejected",
				subnet.isInSubnet(java.net.InetAddress.getByName("192.168.2.1").getAddress()));
	}

	@Test
	public void testSingleIp() throws Exception
	{
		// Test single IP without CIDR prefix (should default to /32 for IPv4)
		Subnet subnet = new Subnet("192.168.1.1");

		assertTrue("Exact match should be accepted",
				subnet.isInSubnet(java.net.InetAddress.getByName("192.168.1.1").getAddress()));

		assertFalse("Different IP should be rejected",
				subnet.isInSubnet(java.net.InetAddress.getByName("192.168.1.2").getAddress()));
	}

	@Test
	public void testSlashInput() throws Exception
	{
		try
		{
			new Subnet("/");
			fail("Expected IllegalArgumentException for '/' input");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
		catch (Exception e)
		{
			fail("Expected IllegalArgumentException, but got " + e.getClass().getName());
		}
	}

	@Test
	public void testEmptyInput() throws Exception
	{
		try
		{
			new Subnet("");
			fail("Expected IllegalArgumentException for empty input");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
		catch (Exception e)
		{
			fail("Expected IllegalArgumentException, but got " + e.getClass().getName());
		}
	}

	@Test
	public void testNullInput() throws Exception
	{
		try
		{
			new Subnet(null);
			fail("Expected IllegalArgumentException for null input");
		}
		catch (IllegalArgumentException e)
		{
			// Expected
		}
		catch (Exception e)
		{
			fail("Expected IllegalArgumentException, but got " + e.getClass().getName());
		}
	}
}
