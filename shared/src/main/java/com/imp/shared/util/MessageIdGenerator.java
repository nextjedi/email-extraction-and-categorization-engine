package com.imp.shared.util;

import com.imp.shared.constant.SourceType;

import java.util.UUID;

/**
 * Utility for generating unique message IDs
 */
public final class MessageIdGenerator {

    private MessageIdGenerator() {
        // Utility class
    }

    /**
     * Generate a unique message ID
     */
    public static String generate(SourceType sourceType, String sourceId) {
        return String.format("%s_%s_%s",
            sourceType.getCode(),
            sourceId,
            UUID.randomUUID().toString().substring(0, 8)
        );
    }

    /**
     * Generate a correlation ID for tracking
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
