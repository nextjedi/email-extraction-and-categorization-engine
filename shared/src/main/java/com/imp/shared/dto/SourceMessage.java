package com.imp.shared.dto;

import com.imp.shared.constant.SourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common message representation across all sources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceMessage {

    private String messageId;
    private String sourceId; // Source-specific ID (Gmail message ID, WhatsApp message ID, etc.)
    private SourceType sourceType;
    private String userId;

    private String subject;
    private String body;
    private String snippet; // Short preview of message

    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;

    private LocalDateTime receivedAt;
    private LocalDateTime sentAt;

    @Builder.Default
    private List<AttachmentDTO> attachments = new ArrayList<>();

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>(); // Source-specific metadata

    private String threadId; // For email threads
    private String conversationId; // For chat conversations

    @Builder.Default
    private List<String> labels = new ArrayList<>(); // Gmail labels, WhatsApp tags, etc.

    private Boolean isRead;
    private Boolean isStarred;
    private Boolean isImportant;
}
