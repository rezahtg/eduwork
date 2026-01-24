-- V000: Base Database Functions
-- Purpose: Create reusable database functions needed by all migrations
-- This must run before any other migrations

-- =============================================================================
-- Function: update_updated_at_column
-- Purpose: Automatically update updated_at timestamp when a row is modified
-- Usage: Called by triggers on tables with updated_at columns
-- =============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- This function will be used by triggers in subsequent migrations
-- Example usage in migrations:
-- CREATE TRIGGER update_users_updated_at
--     BEFORE UPDATE ON users
--     FOR EACH ROW
--     EXECUTE FUNCTION update_updated_at_column();
