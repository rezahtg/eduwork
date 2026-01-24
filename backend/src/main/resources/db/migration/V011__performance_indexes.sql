-- V011: Performance Optimization - Composite Indexes
-- Purpose: Add composite indexes for frequently queried columns
-- Note: Indexes only added for columns that exist in earlier migrations

-- =============================================================================
-- Users Table - Only for columns from V001
-- =============================================================================

-- Index for locked accounts query (from V010)
-- Already created in V010, this is a no-op for safety
CREATE INDEX IF NOT EXISTS idx_users_locked_until_check ON users(id, locked_until)
WHERE locked_until IS NOT NULL;

-- =============================================================================
-- Refresh Tokens - From V006
-- =============================================================================

-- Index for finding active refresh tokens by user
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_active ON refresh_tokens(user_id, expires_at)
WHERE revoked_at IS NULL;

-- Index for token validation
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_lookup ON refresh_tokens(token, expires_at);

-- =============================================================================
-- Email Verification Tokens - From V005
-- =============================================================================

-- Index for email verification token lookup
CREATE INDEX IF NOT EXISTS idx_email_tokens_lookup ON email_verification_tokens(token, expires_at);

-- Index for finding latest verification token by user
CREATE INDEX IF NOT EXISTS idx_email_tokens_user_created ON email_verification_tokens(user_id, created_at DESC);

-- =============================================================================
-- Password Reset Tokens - From V007
-- =============================================================================

-- Index for password reset token lookup
CREATE INDEX IF NOT EXISTS idx_password_reset_lookup ON password_reset_tokens(token, expires_at);

-- Index for finding latest reset token by user
CREATE INDEX IF NOT EXISTS idx_password_reset_user_created ON password_reset_tokens(user_id, created_at DESC);

-- =============================================================================
-- Verification
-- =============================================================================

-- To verify index usage after deployment, run:
-- SELECT schemaname, tablename, indexname, idx_scan
-- FROM pg_stat_user_indexes
-- WHERE indexname LIKE 'idx_%'
-- ORDER BY idx_scan DESC;
