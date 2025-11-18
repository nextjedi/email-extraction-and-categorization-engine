package com.imp.extraction.service;

import com.imp.extraction.entity.ExtractedMessage;
import com.imp.extraction.kafka.MessageExtractedProducer;
import com.imp.extraction.repository.ExtractedMessageRepository;
import com.imp.shared.constant.SourceType;
import com.imp.shared.dto.*;
import com.imp.shared.event.MessageExtractedEvent;
import com.imp.shared.security.AuditLogger;
import com.imp.shared.security.SecurityContext;
import com.imp.shared.strategy.ExtractionStrategy;
import com.imp.shared.util.MessageIdGenerator;
import com.imp.shared.util.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main extraction service that orchestrates message extraction
 * Enhanced with multi-tenant security and data privacy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final List<ExtractionStrategy> extractionStrategies;
    private final ExtractedMessageRepository repository;
    private final MessageExtractedProducer kafkaProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuditLogger auditLogger;

    private static final long DEDUP_TTL_DAYS = 30;

    @Transactional
    public ExtractionResult extractMessages(ExtractionRequest request) {
        // SECURITY: Validate user can only extract their own messages
        if (SecurityContext.isAuthenticated()) {
            SecurityContext.validateUserAccess(request.getUserId());
        }

        log.info("Starting extraction for user {} from source {}",
            request.getUserId(), request.getSourceType());

        // Audit log the extraction request
        auditLogger.logSecurityEvent(AuditLogger.AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(request.getUserId())
            .action("MESSAGE_EXTRACTION_STARTED")
            .metadata("source: " + request.getSourceType())
            .result("IN_PROGRESS")
            .build());

        // Find appropriate strategy
        ExtractionStrategy strategy = extractionStrategies.stream()
            .filter(s -> s.supports(request))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "No extraction strategy found for source: " + request.getSourceType()));

        // Extract messages
        ExtractionResult result = strategy.extract(request);

        // Process and store messages
        int savedCount = 0;
        for (SourceMessage sourceMessage : result.getMessages()) {
            if (isDuplicate(sourceMessage)) {
                log.debug("Skipping duplicate message: {}", sourceMessage.getSourceId());
                continue;
            }

            ExtractedMessage saved = saveMessage(sourceMessage, request.getCorrelationId());
            publishToKafka(saved);
            markInDedup(sourceMessage);
            savedCount++;
        }

        log.info("Extraction completed for user {}. Extracted: {}, Saved: {}",
            request.getUserId(), result.getTotalExtracted(), savedCount);

        return result;
    }

    private boolean isDuplicate(SourceMessage message) {
        // SECURITY: Use user-isolated Redis keys
        String dedupKey = RedisKeyBuilder.buildDedupKey(
            message.getUserId(),
            message.getSourceType().getCode(),
            message.getSourceId()
        );

        Boolean exists = redisTemplate.hasKey(dedupKey);
        if (Boolean.TRUE.equals(exists)) {
            log.debug("Message {} is duplicate (found in Redis cache)", message.getSourceId());
            return true;
        }

        // Also check database (RLS will automatically filter by user_id)
        return repository.existsBySourceIdAndSourceType(message.getSourceId(), message.getSourceType());
    }

    private void markInDedup(SourceMessage message) {
        // SECURITY: Use user-isolated Redis keys
        String dedupKey = RedisKeyBuilder.buildDedupKey(
            message.getUserId(),
            message.getSourceType().getCode(),
            message.getSourceId()
        );
        redisTemplate.opsForValue().set(dedupKey, "1", DEDUP_TTL_DAYS, TimeUnit.DAYS);
        log.debug("Marked message {} as processed in dedup cache", message.getSourceId());
    }

    private ExtractedMessage saveMessage(SourceMessage sourceMessage, String correlationId) {
        ExtractedMessage entity = ExtractedMessage.builder()
            .messageId(MessageIdGenerator.generate(sourceMessage.getSourceType(), sourceMessage.getSourceId()))
            .sourceId(sourceMessage.getSourceId())
            .sourceType(sourceMessage.getSourceType())
            .userId(sourceMessage.getUserId())
            .subject(sourceMessage.getSubject())
            .body(sourceMessage.getBody())
            .snippet(sourceMessage.getSnippet())
            .fromAddress(sourceMessage.getFrom())
            .toAddresses(sourceMessage.getTo() != null ? String.join(",", sourceMessage.getTo()) : null)
            .receivedAt(sourceMessage.getReceivedAt())
            .threadId(sourceMessage.getThreadId())
            .conversationId(sourceMessage.getConversationId())
            .labels(sourceMessage.getLabels() != null ? String.join(",", sourceMessage.getLabels()) : null)
            .isRead(sourceMessage.getIsRead())
            .isStarred(sourceMessage.getIsStarred())
            .publishedToKafka(false)
            .correlationId(correlationId)
            .build();

        return repository.save(entity);
    }

    private void publishToKafka(ExtractedMessage message) {
        RawMessageDTO rawMessage = convertToRawMessage(message);

        MessageExtractedEvent event = MessageExtractedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .message(rawMessage)
            .timestamp(LocalDateTime.now())
            .correlationId(message.getCorrelationId())
            .build();

        kafkaProducer.publishMessageExtracted(event);

        message.setPublishedToKafka(true);
        repository.save(message);
    }

    private RawMessageDTO convertToRawMessage(ExtractedMessage message) {
        return RawMessageDTO.builder()
            .messageId(message.getMessageId())
            .sourceId(message.getSourceId())
            .sourceType(message.getSourceType())
            .userId(message.getUserId())
            .subject(message.getSubject())
            .body(message.getBody())
            .snippet(message.getSnippet())
            .from(message.getFromAddress())
            .to(message.getToAddresses() != null ?
                List.of(message.getToAddresses().split(",")) : List.of())
            .receivedAt(message.getReceivedAt())
            .extractedAt(message.getExtractedAt())
            .threadId(message.getThreadId())
            .conversationId(message.getConversationId())
            .labels(message.getLabels() != null ?
                List.of(message.getLabels().split(",")) : List.of())
            .correlationId(message.getCorrelationId())
            .retryCount(0)
            .build();
    }

    public List<ExtractedMessage> getMessagesByUser(String userId, SourceType sourceType) {
        // SECURITY: Validate user can only access their own messages
        if (SecurityContext.isAuthenticated()) {
            SecurityContext.validateUserAccess(userId);
        }

        auditLogger.logSecurityEvent(AuditLogger.AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .action("MESSAGES_ACCESSED")
            .metadata("source: " + sourceType)
            .result("SUCCESS")
            .build());

        // Database RLS will automatically filter by user_id
        return repository.findByUserIdAndSourceType(userId, sourceType);
    }

    public long getMessageCount(String userId, SourceType sourceType) {
        // SECURITY: Validate user can only access their own count
        if (SecurityContext.isAuthenticated()) {
            SecurityContext.validateUserAccess(userId);
        }

        // Database RLS will automatically filter by user_id
        return repository.countByUserIdAndSourceType(userId, sourceType);
    }

    /**
     * GDPR: Delete all user data
     */
    @Transactional
    public void deleteUserData(String userId, String reason) {
        // SECURITY: Validate user can only delete their own data
        if (SecurityContext.isAuthenticated()) {
            SecurityContext.validateUserAccess(userId);
        }

        log.warn("Deleting all data for user {}: {}", userId, reason);

        // Audit log before deletion
        auditLogger.logDataDeletion(userId, reason);

        // Delete from database (RLS ensures only user's data is deleted)
        List<ExtractedMessage> messages = repository.findByUserIdAndSourceType(userId, null);
        repository.deleteAll(messages);

        // Delete from Redis cache
        String pattern = RedisKeyBuilder.buildUserPattern(userId);
        redisTemplate.keys(pattern).forEach(key -> {
            if (RedisKeyBuilder.isUserKey(key, userId)) {
                redisTemplate.delete(key);
            }
        });

        log.info("Successfully deleted all data for user {}", userId);
    }
}
