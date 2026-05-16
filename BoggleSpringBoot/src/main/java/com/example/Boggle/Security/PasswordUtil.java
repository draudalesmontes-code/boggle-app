package com.example.Boggle.Security;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Utility class for hashing and verifying passwords.
 *
 * <p>This implementation hashes passwords with SHA-256 and encodes the
 * resulting hash in Base64 for storage and comparison.
 */
public final class PasswordUtil {

    /**
     * Prevents instantiation of this utility class.
     */
    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param password the plain-text password to hash
     * @return the Base64-encoded SHA-256 hash of the password
     * @throws IllegalArgumentException if the password is null or blank
     */
    public static String hash(String password){
        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("Password Required");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored password hash.
     *
     * @param password the plain-text password to verify
     * @param stored the stored Base64-encoded password hash
     * @return {@code true} if the password matches the stored hash;
     *         {@code false} otherwise
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }

        try {
            String hashedInput = hash(password);

            return MessageDigest.isEqual(
                    hashedInput.getBytes(),
                    stored.getBytes()
            );
        } catch (Exception e) {
            return false;
        }
    }

}
