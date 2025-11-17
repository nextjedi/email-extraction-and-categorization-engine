package com.imp.classification.service;

import com.imp.classification.entity.ClassifiedMessage;
import com.imp.classification.kafka.MessageClassifiedProducer;
import com.imp.classification.repository.ClassifiedMessageRepository;
import com.imp.shared.constant.KafkaTopics;
import com.imp.shared.constant.MessageCategory;
import com.imp.shared.dto.ClassificationResult;
import com.imp.shared.dto.RawMessageDTO;
import com.imp.shared.event.MessageClassifiedEvent;
import com.imp.shared.strategy.ClassificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Main classification service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final List<ClassificationStrategy> classificationStrategies;
    private final ClassifiedMessageRepository repository;
    private final MessageClassifiedProducer kafkaProducer;
    private final RedisTemplate<String, ClassificationResult> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "classification:";
    private static final long CACHE_TTL_DAYS = 7;

    @Transactional
    public void classifyAndPublish(RawMessageDTO message) {
        log.debug("Classifying message: {}", message.getMessageId());

        // Check cache first
        ClassificationResult cached = getCachedClassification(message.getMessageId());
        if (cached != null) {
            log.debug("Using cached classification for message: {}", message.getMessageId());
            publishClassification(message, cached);
            return;
        }

        // Use strategy with highest priority
        ClassificationStrategy strategy = classificationStrategies.stream()
            .max((s1, s2) -> Integer.compare(s1.getPriority(), s2.getPriority()))
            .orElseThrow(() -> new IllegalStateException("No classification strategy available"));

        ClassificationResult result = strategy.classify(message);

        // Save to database
        saveClassification(result);

        // Cache result
        cacheClassification(result);

        // Publish to appropriate Kafka topic
        publishClassification(message, result);

        log.info("Message {} classified as {} with confidence {}",
            message.getMessageId(), result.getPrimaryCategory(), result.getConfidence());
    }

    private void saveClassification(ClassificationResult result) {
        ClassifiedMessage entity = ClassifiedMessage.builder()
            .messageId(result.getMessageId())
            .userId(result.getUserId())
            .primaryCategory(result.getPrimaryCategory())
            .confidence(result.getConfidence())
            .classifierName(result.getClassifierName())
            .classifiedAt(result.getClassifiedAt())
            .correlationId(result.getCorrelationId())
            .build();

        repository.save(entity);
    }

    private void publishClassification(RawMessageDTO message, ClassificationResult result) {
        MessageClassifiedEvent event = MessageClassifiedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .rawMessage(message)
            .classification(result)
            .timestamp(LocalDateTime.now())
            .correlationId(result.getCorrelationId())
            .build();

        String topic = KafkaTopics.getClassifiedTopic(result.getPrimaryCategory());
        kafkaProducer.publishMessageClassified(topic, event);
    }

    private ClassificationResult getCachedClassification(String messageId) {
        String key = CACHE_KEY_PREFIX + messageId;
        return redisTemplate.opsForValue().get(key);
    }

    private void cacheClassification(ClassificationResult result) {
        String key = CACHE_KEY_PREFIX + result.getMessageId();
        redisTemplate.opsForValue().set(key, result, CACHE_TTL_DAYS, TimeUnit.DAYS);
    }

    public List<ClassifiedMessage> getClassificationsByCategory(MessageCategory category) {
        return repository.findByPrimaryCategory(category);
    }

    public long getCountByCategory(MessageCategory category) {
        return repository.countByPrimaryCategory(category);
    }
}
