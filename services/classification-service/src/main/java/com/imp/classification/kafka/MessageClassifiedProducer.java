package com.imp.classification.kafka;

import com.imp.shared.event.MessageClassifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageClassifiedProducer {

    private final KafkaTemplate<String, MessageClassifiedEvent> kafkaTemplate;

    public void publishMessageClassified(String topic, MessageClassifiedEvent event) {
        log.debug("Publishing message classified event to topic {}: {}", topic, event.getEventId());

        kafkaTemplate.send(topic, event.getEventId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message classified event published to topic {}: {}", topic, event.getEventId());
                } else {
                    log.error("Failed to publish message classified event: {}", event.getEventId(), ex);
                }
            });
    }
}
