package com.imp.extraction.kafka;

import com.imp.shared.constant.KafkaTopics;
import com.imp.shared.event.MessageExtractedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for message extracted events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageExtractedProducer {

    private final KafkaTemplate<String, MessageExtractedEvent> kafkaTemplate;

    public void publishMessageExtracted(MessageExtractedEvent event) {
        log.debug("Publishing message extracted event: {}", event.getEventId());

        CompletableFuture<SendResult<String, MessageExtractedEvent>> future =
            kafkaTemplate.send(KafkaTopics.RAW_MESSAGES_EXTRACTED, event.getEventId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message extracted event published successfully: {} to partition {}",
                    event.getEventId(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish message extracted event: {}", event.getEventId(), ex);
            }
        });
    }
}
