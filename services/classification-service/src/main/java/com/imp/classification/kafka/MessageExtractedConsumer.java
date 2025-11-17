package com.imp.classification.kafka;

import com.imp.classification.service.ClassificationService;
import com.imp.shared.constant.KafkaTopics;
import com.imp.shared.event.MessageExtractedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for message extracted events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageExtractedConsumer {

    private final ClassificationService classificationService;

    @KafkaListener(
        topics = KafkaTopics.RAW_MESSAGES_EXTRACTED,
        groupId = "classification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessageExtracted(MessageExtractedEvent event) {
        log.info("Received message extracted event: {}", event.getEventId());

        try {
            classificationService.classifyAndPublish(event.getMessage());
            log.info("Successfully classified message: {}", event.getMessage().getMessageId());
        } catch (Exception e) {
            log.error("Error classifying message: {}", event.getMessage().getMessageId(), e);
            // TODO: Publish to DLQ
        }
    }
}
