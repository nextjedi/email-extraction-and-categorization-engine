package com.imp.gmail.service;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.imp.gmail.client.GmailApiClient;
import com.imp.shared.api.SourceMessagesApi;
import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.AttachmentDTO;
import com.imp.shared.dto.SourceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer that orchestrates Gmail API client
 * Implements SourceMessagesApi interface
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GmailMessagesService implements SourceMessagesApi {

    private final GmailApiClient gmailApiClient;

    @Override
    public List<SourceMessage> fetchMessages(String userId, LocalDateTime from, LocalDateTime to) {
        log.info("Fetching Gmail messages for user {} from {} to {}", userId, from, to);

        String query = buildDateQuery(from, to);
        List<Message> gmailMessages = gmailApiClient.listMessages(userId, query, 100);

        return gmailMessages.stream()
            .map(msg -> convertToSourceMessage(userId, msg))
            .collect(Collectors.toList());
    }

    @Override
    public IncrementalFetchResult fetchIncremental(String userId, String syncToken) {
        // Gmail doesn't have sync tokens like Calendar API
        // Using history API would be the proper implementation
        // For now, fetch recent messages
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);

        List<SourceMessage> messages = fetchMessages(userId, oneDayAgo, now);

        return new IncrementalFetchResult(
            messages,
            UUID.randomUUID().toString(), // Mock sync token
            false
        );
    }

    @Override
    @Cacheable(value = "gmail-messages", key = "#userId + '-' + #sourceMessageId")
    public SourceMessage getMessageById(String userId, String sourceMessageId) {
        log.info("Getting Gmail message {} for user {}", sourceMessageId, userId);

        Message gmailMessage = gmailApiClient.getMessage(userId, sourceMessageId);
        return convertToSourceMessage(userId, gmailMessage);
    }

    @Override
    public String sendMessage(String userId, SendMessageRequest request) {
        log.info("Sending Gmail message for user {} to {}", userId, request.to());

        // Build Gmail message
        // In production, properly encode MIME message
        Message gmailMessage = new Message();
        // TODO: Implement proper MIME message creation

        Message sent = gmailApiClient.sendMessage(userId, gmailMessage);
        return sent.getId();
    }

    @Override
    public void markAsRead(String userId, String sourceMessageId) {
        log.info("Marking Gmail message {} as read for user {}", sourceMessageId, userId);

        gmailApiClient.modifyMessage(
            userId,
            sourceMessageId,
            Collections.emptyList(),
            List.of("UNREAD")
        );
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.GMAIL;
    }

    @Override
    public boolean isAvailable(String userId) {
        try {
            gmailApiClient.getUserProfile(userId);
            return true;
        } catch (Exception e) {
            log.error("Gmail not available for user {}", userId, e);
            return false;
        }
    }

    /**
     * Convert Gmail Message to SourceMessage
     */
    private SourceMessage convertToSourceMessage(String userId, Message gmailMessage) {
        MessagePart payload = gmailMessage.getPayload();
        Map<String, String> headers = extractHeaders(payload);

        return SourceMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .sourceId(gmailMessage.getId())
            .sourceType(SourceType.GMAIL)
            .userId(userId)
            .subject(headers.get("Subject"))
            .body(extractBody(payload))
            .snippet(gmailMessage.getSnippet())
            .from(headers.get("From"))
            .to(parseRecipients(headers.get("To")))
            .cc(parseRecipients(headers.get("Cc")))
            .bcc(parseRecipients(headers.get("Bcc")))
            .receivedAt(convertTimestamp(gmailMessage.getInternalDate()))
            .sentAt(convertTimestamp(gmailMessage.getInternalDate()))
            .threadId(gmailMessage.getThreadId())
            .labels(gmailMessage.getLabelIds() != null ? gmailMessage.getLabelIds() : new ArrayList<>())
            .attachments(extractAttachments(payload))
            .isRead(!gmailMessage.getLabelIds().contains("UNREAD"))
            .isStarred(gmailMessage.getLabelIds().contains("STARRED"))
            .isImportant(gmailMessage.getLabelIds().contains("IMPORTANT"))
            .build();
    }

    private Map<String, String> extractHeaders(MessagePart payload) {
        if (payload == null || payload.getHeaders() == null) {
            return new HashMap<>();
        }

        return payload.getHeaders().stream()
            .collect(Collectors.toMap(
                MessagePartHeader::getName,
                MessagePartHeader::getValue,
                (v1, v2) -> v1
            ));
    }

    private String extractBody(MessagePart payload) {
        if (payload == null) {
            return "";
        }

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return new String(Base64.getUrlDecoder().decode(payload.getBody().getData()));
        }

        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if ("text/plain".equals(part.getMimeType()) || "text/html".equals(part.getMimeType())) {
                    if (part.getBody() != null && part.getBody().getData() != null) {
                        return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                    }
                }
            }
        }

        return "";
    }

    private List<AttachmentDTO> extractAttachments(MessagePart payload) {
        List<AttachmentDTO> attachments = new ArrayList<>();

        if (payload == null || payload.getParts() == null) {
            return attachments;
        }

        for (MessagePart part : payload.getParts()) {
            if (part.getFilename() != null && !part.getFilename().isEmpty()) {
                attachments.add(AttachmentDTO.builder()
                    .attachmentId(part.getBody().getAttachmentId())
                    .filename(part.getFilename())
                    .mimeType(part.getMimeType())
                    .size(part.getBody().getSize() != null ? part.getBody().getSize().longValue() : 0L)
                    .build());
            }
        }

        return attachments;
    }

    private List<String> parseRecipients(String recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(recipients.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private LocalDateTime convertTimestamp(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }

    private String buildDateQuery(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return String.format("after:%s before:%s",
            from.format(formatter),
            to.format(formatter)
        );
    }
}
