package io.openleap.iam.principal.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for generating and hashing API keys.
 */
@Service
public class CredentialService {
    
    private static final int API_KEY_LENGTH = 32;
    private static final String API_KEY_PREFIX = "sk_live_";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generates a secure random API key.
     * Format: sk_live_{32 random bytes base64 encoded}
     * 
     * @return the generated API key
     */
    public String generateApiKey() {
        byte[] randomBytes = new byte[API_KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return API_KEY_PREFIX + encoded;
    }
    
    /**
     * Hashes an API key using SHA-256.
     * 
     * @param apiKey the plain-text API key
     * @return the SHA-256 hash (hex encoded, 64 characters)
     */
    public String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Converts byte array to hexadecimal string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
