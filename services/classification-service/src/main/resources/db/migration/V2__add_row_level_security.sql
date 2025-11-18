-- V2: Add Row-Level Security for multi-tenant data isolation

-- Enable Row-Level Security on classified_messages
ALTER TABLE classified_messages ENABLE ROW LEVEL SECURITY;

-- Create policy: Users can only see their own classified messages
CREATE POLICY user_isolation_policy ON classified_messages
    FOR SELECT
    USING (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only insert their own classifications
CREATE POLICY user_insert_policy ON classified_messages
    FOR INSERT
    WITH CHECK (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only update their own classifications
CREATE POLICY user_update_policy ON classified_messages
    FOR UPDATE
    USING (user_id = current_setting('app.current_user_id', true)::text)
    WITH CHECK (user_id = current_setting('app.current_user_id', true)::text);

-- Create policy: Users can only delete their own classifications
CREATE POLICY user_delete_policy ON classified_messages
    FOR DELETE
    USING (user_id = current_setting('app.current_user_id', true)::text);

-- Function to set user context
CREATE OR REPLACE FUNCTION set_current_user_id(p_user_id text)
RETURNS void AS $$
BEGIN
    PERFORM set_config('app.current_user_id', p_user_id, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION set_current_user_id(text) TO imp_user;

-- Create index on user_id for better RLS performance
CREATE INDEX IF NOT EXISTS idx_user_id_perf ON classified_messages(user_id)
    WHERE user_id IS NOT NULL;

COMMENT ON TABLE classified_messages IS
'Table with Row-Level Security enabled. Users can only access their own classifications.';

-- Create audit table
CREATE TABLE IF NOT EXISTS classification_access_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    success BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_classification_audit_user_time ON classification_access_audit(user_id, accessed_at DESC);

COMMENT ON TABLE classification_access_audit IS
'Audit log for classification access. Required for GDPR compliance.';
