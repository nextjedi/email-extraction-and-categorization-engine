package com.imp.jobsearch.service;

import com.imp.shared.event.MessageClassifiedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Processes job search related messages
 */
@Slf4j
@Service
public class JobSearchProcessingService {

    public void process(MessageClassifiedEvent event) {
        log.info("Processing job search message: {}", event.getRawMessage().getMessageId());

        // TODO: Implement job search specific processing:
        // - Extract company names
        // - Extract job titles
        // - Extract salary information
        // - Track application status
        // - Store in job search specific schema

        log.info("Job search message processed successfully");
    }
}
