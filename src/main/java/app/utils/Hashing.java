package app.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Hashing {

    /**
     * This method hashes data from ScrapedData entity, so it becomes a fingerprint in the DB
     * In order to check for duplicate scrape data
     */
    public static String sha256(String input) {
        if (input == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert byte[] to lowercase hex string
            StringBuilder hex = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 hash", e);
        }
    }
}

