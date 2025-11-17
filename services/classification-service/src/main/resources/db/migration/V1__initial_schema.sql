-- Initial schema for classification service

CREATE TABLE IF NOT EXISTS classified_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    primary_category VARCHAR(50) NOT NULL,
    confidence DOUBLE PRECISION,
    classifier_name VARCHAR(100) NOT NULL,
    classified_at TIMESTAMP NOT NULL,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_category ON classified_messages(primary_category);
CREATE INDEX idx_user_category ON classified_messages(user_id, primary_category);
CREATE INDEX idx_classified_at ON classified_messages(classified_at);

-- Comments
COMMENT ON TABLE classified_messages IS 'Stores classification results for messages';
COMMENT ON COLUMN classified_messages.primary_category IS 'Primary category assigned to the message';
COMMENT ON COLUMN classified_messages.confidence IS 'Confidence score of the classification (0-1)';
COMMENT ON COLUMN classified_messages.classifier_name IS 'Name of the classifier used (e.g., rule-based, ml, hybrid)';
