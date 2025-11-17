package com.imp.jobsearch.kafka;

import com.imp.jobsearch.service.JobSearchProcessingService;
import com.imp.shared.constant.KafkaTopics;
import com.imp.shared.event.MessageClassifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSearchMessageConsumer {

    private final JobSearchProcessingService processingService;

    @KafkaListener(
        topics = KafkaTopics.CLASSIFIED_JOB_SEARCH,
        groupId = "job-search-processor-group"
    )
    public void consumeJobSearchMessage(MessageClassifiedEvent event) {
        log.info("Received job search message: {}", event.getEventId());
        try {
            processingService.process(event);
        } catch (Exception e) {
            log.error("Error processing job search message: {}", event.getEventId(), e);
        }
    }
}
