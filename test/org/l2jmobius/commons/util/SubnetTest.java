package org.l2jmobius.commons.util;

import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;

public class SubnetTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() throws UnknownHostException {
        new Subnet(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmpty() throws UnknownHostException {
        new Subnet("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWhitespace() throws UnknownHostException {
        new Subnet("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSlashOnly() throws UnknownHostException {
        new Subnet("/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSlashStart() throws UnknownHostException {
        new Subnet("/24");
    }

    @Test(expected = UnknownHostException.class)
    public void testConstructorInvalidIP() throws UnknownHostException {
        new Subnet("999.999.999.999");
    }

    @Test
    public void testConstructorValidIPv4() throws UnknownHostException {
        Subnet s = new Subnet("127.0.0.1/24");
        assertNotNull(s);
        assertTrue(s.isInSubnet(java.net.InetAddress.getByName("127.0.0.5").getAddress()));
    }

    @Test
    public void testConstructorValidIPv4NoMask() throws UnknownHostException {
        Subnet s = new Subnet("127.0.0.1"); // defaults to /32
        assertNotNull(s);
        assertTrue(s.isInSubnet(java.net.InetAddress.getByName("127.0.0.1").getAddress()));
        assertFalse(s.isInSubnet(java.net.InetAddress.getByName("127.0.0.2").getAddress()));
    }

    @Test
    public void testConstructorValidIPv6() throws UnknownHostException {
        Subnet s = new Subnet("2001:db8::/32");
        assertNotNull(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIPv4PrefixTooLarge() throws UnknownHostException {
        new Subnet("127.0.0.1/33");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIPv4PrefixNegative() throws UnknownHostException {
        new Subnet("127.0.0.1/-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIPv6PrefixTooLarge() throws UnknownHostException {
        new Subnet("2001:db8::/129");
    }
}
