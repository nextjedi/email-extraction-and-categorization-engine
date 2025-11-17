package com.imp.shared.api;

import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.SourceMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Common interface for all source message services
 * Abstracts away source-specific implementations
 */
public interface SourceMessagesApi {

    /**
     * Fetch messages from the source within a date range
     */
    List<SourceMessage> fetchMessages(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Fetch messages incrementally using sync token
     */
    IncrementalFetchResult fetchIncremental(String userId, String syncToken);

    /**
     * Get a specific message by its source ID
     */
    SourceMessage getMessageById(String userId, String sourceMessageId);

    /**
     * Send a message (if supported by the source)
     */
    String sendMessage(String userId, SendMessageRequest request);

    /**
     * Mark message as read
     */
    void markAsRead(String userId, String sourceMessageId);

    /**
     * Get the source type this API handles
     */
    SourceType getSourceType();

    /**
     * Check if the source is available and properly configured
     */
    boolean isAvailable(String userId);

    /**
     * Result of incremental fetch
     */
    record IncrementalFetchResult(
            List<SourceMessage> messages,
            String nextSyncToken,
            boolean hasMore
    ) {}

    /**
     * Request to send a message
     */
    record SendMessageRequest(
            String to,
            String subject,
            String body,
            List<String> cc,
            List<String> bcc
    ) {}
}
