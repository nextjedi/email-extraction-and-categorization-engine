package com.imp.shared.constant;

/**
 * Kafka topic names used across the system
 */
public final class KafkaTopics {

    // Raw messages extracted from sources
    public static final String RAW_MESSAGES_EXTRACTED = "raw-messages.extracted";

    // Classified messages by category
    public static final String CLASSIFIED_TRANSACTIONAL = "classified.transactional";
    public static final String CLASSIFIED_JOB_SEARCH = "classified.job-search";
    public static final String CLASSIFIED_SUBSCRIPTION = "classified.subscription";
    public static final String CLASSIFIED_PERSONAL = "classified.personal";
    public static final String CLASSIFIED_TRAVEL = "classified.travel";
    public static final String CLASSIFIED_OTHER = "classified.other";

    // Dead letter topics
    public static final String DLQ_EXTRACTION = "dlq.extraction";
    public static final String DLQ_CLASSIFICATION = "dlq.classification";
    public static final String DLQ_PROCESSING = "dlq.processing";

    private KafkaTopics() {
        // Utility class
    }

    public static String getClassifiedTopic(MessageCategory category) {
        return switch (category) {
            case TRANSACTIONAL -> CLASSIFIED_TRANSACTIONAL;
            case JOB_SEARCH -> CLASSIFIED_JOB_SEARCH;
            case SUBSCRIPTION -> CLASSIFIED_SUBSCRIPTION;
            case PERSONAL -> CLASSIFIED_PERSONAL;
            case TRAVEL -> CLASSIFIED_TRAVEL;
            default -> CLASSIFIED_OTHER;
        };
    }
}
