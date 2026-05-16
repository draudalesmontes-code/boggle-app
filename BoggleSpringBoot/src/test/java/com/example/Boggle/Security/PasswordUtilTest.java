package com.example.Boggle.Security;

import com.example.Boggle.Security.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the PasswordUtil.java
 *
 * These tests validate the behavior of password hashing and verification
 * using the SHA-256 based implementation provided in PasswordUtil.
 *
 * The tests cover:
 * - Successful hashing
 * - Deterministic hashing behavior
 * - Exception handling for invalid inputs
 * - Successful password verification
 * - Failed verification scenarios
 * - Null input handling
 */
@DisplayName("Password Utility Tests")
class PasswordUtilTest {

    /**
     * Verifies that the hash method returns a non-null, non-blank
     * Base64-encoded hash when provided with a valid password.
     */
    @Test
    void hash_WhenPasswordValid() {
        String password = "securePassword123";

        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }

    /**
     * Verifies that hashing the same password multiple times produces
     * the same hash value since the implementation does not use salting.
     */
    @Test
    void hash_ForSamePassword() {
        String password = "samePassword";

        String hash1 = PasswordUtil.hash(password);
        String hash2 = PasswordUtil.hash(password);

        assertEquals(hash1, hash2);
    }

    /**
     * Ensures that the hash method throws an IllegalArgumentException
     * when a null password is provided.
     */
    @Test
    void hash_WhenPasswordNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hash(null);
        });
    }

    /**
     * Ensures that the hash method throws an IllegalArgumentException
     * when a blank password is provided.
     */
    @Test
    void hash_WhenPasswordBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hash("   ");
        });
    }

    /**
     * Verifies that the verify method returns true when the provided
     * plaintext password matches the stored hashed password.
     */
    @Test
    void verify_WhenPasswordMatches() {
        String password = "myPassword";
        String storedHash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.verify(password, storedHash);

        assertTrue(result);
    }

    /**
     * Verifies that the verify method returns false when the provided
     * password does not match the stored hash.
     */
    @Test
    void verify_WhenPasswordIncorrect() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";

        String storedHash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.verify(wrongPassword, storedHash);

        assertFalse(result);
    }

    /**
     * Ensures that the verify method returns false when the provided
     * password is null.
     */
    @Test
    void verify_WhenPasswordNull() {
        String storedHash = PasswordUtil.hash("password");

        boolean result = PasswordUtil.verify(null, storedHash);

        assertFalse(result);
    }

    /**
     * Ensures that the verify method returns false when the stored
     * hash is null.
     */
    @Test
    void verify_WhenStoredHashNull() {
        boolean result = PasswordUtil.verify("password", null);

        assertFalse(result);
    }
}