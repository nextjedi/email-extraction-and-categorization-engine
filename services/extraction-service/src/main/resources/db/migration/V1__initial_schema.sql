-- Initial schema for extraction service

CREATE TABLE IF NOT EXISTS extracted_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    source_id VARCHAR(255) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    subject VARCHAR(1000),
    body TEXT,
    snippet VARCHAR(500),
    from_address VARCHAR(500),
    to_addresses TEXT,
    received_at TIMESTAMP,
    extracted_at TIMESTAMP NOT NULL,
    thread_id VARCHAR(255),
    conversation_id VARCHAR(255),
    labels TEXT,
    is_read BOOLEAN,
    is_starred BOOLEAN,
    published_to_kafka BOOLEAN NOT NULL DEFAULT FALSE,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_user_source ON extracted_messages(user_id, source_type);
CREATE INDEX idx_source_id ON extracted_messages(source_id);
CREATE INDEX idx_extracted_at ON extracted_messages(extracted_at);
CREATE INDEX idx_published_to_kafka ON extracted_messages(published_to_kafka) WHERE published_to_kafka = FALSE;
CREATE UNIQUE INDEX idx_source_unique ON extracted_messages(source_id, source_type);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_extracted_messages_updated_at BEFORE UPDATE
    ON extracted_messages FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE extracted_messages IS 'Stores all extracted messages from various sources';
COMMENT ON COLUMN extracted_messages.message_id IS 'Internal unique message identifier';
COMMENT ON COLUMN extracted_messages.source_id IS 'Source-specific message ID';
COMMENT ON COLUMN extracted_messages.published_to_kafka IS 'Flag indicating if message was published to Kafka';
