-- V2: Add Row-Level Security for multi-tenant data isolation

-- Enable Row-Level Security on extracted_messages
ALTER TABLE extracted_messages ENABLE ROW LEVEL SECURITY;

-- Create policy: Users can only see their own messages
CREATE POLICY user_isolation_policy ON extracted_messages
    FOR SELECT
    USING (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only insert their own messages
CREATE POLICY user_insert_policy ON extracted_messages
    FOR INSERT
    WITH CHECK (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only update their own messages
CREATE POLICY user_update_policy ON extracted_messages
    FOR UPDATE
    USING (user_id = current_setting('app.current_user_id', true)::text)
    WITH CHECK (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only delete their own messages
CREATE POLICY user_delete_policy ON extracted_messages
    FOR DELETE
    USING (user_id = current_setting('app.current_user_id', true)::text);

-- Function to set user context (called by application)
CREATE OR REPLACE FUNCTION set_current_user_id(p_user_id text)
RETURNS void AS $$
BEGIN
    PERFORM set_config('app.current_user_id', p_user_id, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission to application user
GRANT EXECUTE ON FUNCTION set_current_user_id(text) TO imp_user;

-- Create index on user_id for better RLS performance
CREATE INDEX IF NOT EXISTS idx_user_id_perf ON extracted_messages(user_id)
    WHERE user_id IS NOT NULL;

-- Add comment explaining RLS
COMMENT ON TABLE extracted_messages IS
'Table with Row-Level Security enabled. Users can only access their own messages.';

-- Create audit table for tracking access
CREATE TABLE IF NOT EXISTS message_access_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    success BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_audit_user_time ON message_access_audit(user_id, accessed_at DESC);
CREATE INDEX idx_audit_message ON message_access_audit(message_id);

COMMENT ON TABLE message_access_audit IS
'Audit log for all message access attempts. Required for GDPR compliance.';
