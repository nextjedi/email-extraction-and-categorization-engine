package com.imp.shared.util;

/**
 * Utility for building user-isolated Redis keys
 */
public class RedisKeyBuilder {

    private static final String SEPARATOR = ":";
    private static final String USER_PREFIX = "user";

    /**
     * Build a user-specific key
     */
    public static String buildKey(String userId, String... parts) {
        StringBuilder key = new StringBuilder();
        key.append(USER_PREFIX)
           .append(SEPARATOR)
           .append(userId);

        for (String part : parts) {
            key.append(SEPARATOR).append(part);
        }

        return key.toString();
    }

    /**
     * Build deduplication key
     */
    public static String buildDedupKey(String userId, String sourceType, String sourceId) {
        return buildKey(userId, "dedup", sourceType, sourceId);
    }

    /**
     * Build classification cache key
     */
    public static String buildClassificationKey(String userId, String messageId) {
        return buildKey(userId, "classification", messageId);
    }

    /**
     * Build contact cache key
     */
    public static String buildContactKey(String userId) {
        return buildKey(userId, "contacts");
    }

    /**
     * Build rate limit key
     */
    public static String buildRateLimitKey(String userId, String operation) {
        return buildKey(userId, "ratelimit", operation);
    }

    /**
     * Build session key
     */
    public static String buildSessionKey(String userId, String sessionId) {
        return buildKey(userId, "session", sessionId);
    }

    /**
     * Extract userId from a Redis key
     */
    public static String extractUserId(String key) {
        String[] parts = key.split(SEPARATOR);
        if (parts.length >= 2 && USER_PREFIX.equals(parts[0])) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid user key format: " + key);
    }

    /**
     * Validate that a key belongs to a user
     */
    public static boolean isUserKey(String key, String userId) {
        try {
            String extractedUserId = extractUserId(key);
            return userId.equals(extractedUserId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Build pattern for scanning user's keys
     */
    public static String buildUserPattern(String userId) {
        return buildKey(userId, "*");
    }
}
