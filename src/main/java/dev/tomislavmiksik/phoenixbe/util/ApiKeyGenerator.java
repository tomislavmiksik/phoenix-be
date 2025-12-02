package dev.tomislavmiksik.phoenixbe.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * The type Api key generator.
 */
public class ApiKeyGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generate raw key string.
     *
     * @return the raw key
     */
    public static String generateKey() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }
}
