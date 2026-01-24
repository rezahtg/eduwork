-- V010: Add Brute Force Protection Support
-- Add locked_until column to users table for account locking
-- Note: login_attempts table already exists from V004

-- Add locked_until column to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;

-- Add index for locked accounts query
CREATE INDEX IF NOT EXISTS idx_users_locked_until ON users(locked_until)
    WHERE locked_until IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN users.locked_until IS 'Account locked until this timestamp due to failed login attempts';
COMMENT ON TABLE login_attempts IS 'Audit trail of all login attempts for security monitoring (created in V004)';
