-- V7: Add Brute Force Protection Support
-- Add locked_until column to users table for account locking
-- Add login_attempts table for audit trail

-- Add locked_until column to users table
ALTER TABLE users
    ADD COLUMN locked_until TIMESTAMP;

-- Add index for locked accounts query
CREATE INDEX idx_users_locked_until ON users(locked_until)
    WHERE locked_until IS NOT NULL;

-- Create login_attempts table for audit trail
CREATE TABLE login_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    successful BOOLEAN NOT NULL,
    failure_reason VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for audit queries
CREATE INDEX idx_login_attempts_email ON login_attempts(email, attempted_at DESC);
CREATE INDEX idx_login_attempts_ip ON login_attempts(ip_address, attempted_at DESC);
CREATE INDEX idx_login_attempts_time ON login_attempts(attempted_at);

-- Add comments for documentation
COMMENT ON COLUMN users.locked_until IS 'Account locked until this timestamp due to failed login attempts';
COMMENT ON TABLE login_attempts IS 'Audit trail of all login attempts for security monitoring';
COMMENT ON COLUMN login_attempts.email IS 'Email address used in login attempt';
COMMENT ON COLUMN login_attempts.ip_address IS 'IP address of the login attempt';
COMMENT ON COLUMN login_attempts.successful IS 'Whether the login attempt was successful';
COMMENT ON COLUMN login_attempts.failure_reason IS 'Reason for login failure (e.g., INVALID_CREDENTIALS, ACCOUNT_LOCKED)';
