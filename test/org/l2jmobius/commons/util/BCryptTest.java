package org.l2jmobius.commons.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class BCryptTest {
    @Test
    public void testHashAndCheck() {
        String password = "mysecretpassword";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        assertTrue("Password should match hash", BCrypt.checkpw(password, hashed));
        assertFalse("Different password should not match", BCrypt.checkpw("wrongpassword", hashed));
    }

    @Test
    public void testSaltGeneration() {
        String salt1 = BCrypt.gensalt();
        String salt2 = BCrypt.gensalt();
        assertNotEquals("Salts should be different", salt1, salt2);
    }

    @Test
    public void testWorkFactor() {
        // Test with a low work factor for speed
        String password = "password";
        String salt = BCrypt.gensalt(4); // Minimum rounds
        String hashed = BCrypt.hashpw(password, salt);
        assertTrue(BCrypt.checkpw(password, hashed));
    }
}
