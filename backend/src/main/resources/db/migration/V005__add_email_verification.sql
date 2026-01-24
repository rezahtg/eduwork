-- Add email verification support to users table
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Create email_verification_tokens table
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index for faster token lookup
CREATE INDEX idx_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_verification_user_created ON email_verification_tokens(user_id, created_at);

-- Add comment for documentation
COMMENT ON TABLE email_verification_tokens IS 'Email verification tokens for user registration. Tokens expire after 15 minutes and are single-use.';
COMMENT ON COLUMN email_verification_tokens.token IS 'Unique verification token (UUID string)';
COMMENT ON COLUMN email_verification_tokens.expires_at IS 'Token expiration timestamp (15 minutes from creation)';
COMMENT ON COLUMN email_verification_tokens.used_at IS 'Timestamp when token was used (null if not yet used)';
