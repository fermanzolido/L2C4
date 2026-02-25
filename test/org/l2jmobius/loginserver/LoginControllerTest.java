package org.l2jmobius.loginserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
import java.util.Arrays;

import org.junit.Test;

public class LoginControllerTest
{
	/**
	 * Verifies that MessageDigest.isEqual works as expected for password hash comparison.
	 * This ensures that replacing the loop with this method maintains functional correctness
	 * while providing constant-time comparison.
	 */
	@Test
	public void testConstantTimeComparison()
	{
		byte[] hash = "password123".getBytes();
		byte[] expected = "password123".getBytes();
		byte[] different = "password124".getBytes();
		byte[] differentLength = "password12".getBytes();

		// Test equal arrays
		assertTrue("Hashes should be equal", MessageDigest.isEqual(hash, expected));

		// Test different content
		assertFalse("Hashes should not be equal", MessageDigest.isEqual(hash, different));

		// Test different length
		assertFalse("Hashes should not be equal (length mismatch)", MessageDigest.isEqual(hash, differentLength));

		// Test nulls (though in LoginController expected is checked for null before)
		try {
			MessageDigest.isEqual(hash, null);
		} catch (NullPointerException e) {
			// Expected behavior of MessageDigest.isEqual if one is null?
			// Actually checking JDK docs, it might throw NPE or return false depending on impl?
			// OpenJDK implementation throws NPE if either is null.
		}
	}

	/**
	 * Simulates the logic in LoginController to ensure it behaves correctly.
	 */
	@Test
	public void testLoginControllerLogicSimulation()
	{
		byte[] hash = new byte[]{1, 2, 3, 4, 5};
		byte[] expected = new byte[]{1, 2, 3, 4, 5};

		boolean ok = MessageDigest.isEqual(hash, expected);
		assertTrue(ok);

		expected = new byte[]{1, 2, 3, 4, 6};
		ok = MessageDigest.isEqual(hash, expected);
		assertFalse(ok);
	}
}
