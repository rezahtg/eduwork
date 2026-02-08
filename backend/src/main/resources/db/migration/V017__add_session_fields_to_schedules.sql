-- V017__add_session_fields_to_schedules.sql
-- 
-- Purpose: Add session-related fields to schedules table to support
-- automatic session generation from schedules.
--
-- This bridges the OLD schedule system (with TimeSlots) and NEW session system
-- (with bookable sessions).

-- Add session configuration fields to schedules
ALTER TABLE schedules
ADD COLUMN subject VARCHAR(255),
ADD COLUMN session_type VARCHAR(20),  -- ONE_ON_ONE or GROUP
ADD COLUMN min_students INTEGER DEFAULT 1,
ADD COLUMN max_students INTEGER DEFAULT 1,
ADD COLUMN price_amount DECIMAL(19,4),
ADD COLUMN price_currency VARCHAR(3) DEFAULT 'IDR';

-- Add constraints
ALTER TABLE schedules
ADD CONSTRAINT chk_session_type 
    CHECK (session_type IN ('ONE_ON_ONE', 'GROUP'));

ALTER TABLE schedules
ADD CONSTRAINT chk_capacity 
    CHECK (min_students >= 1 AND max_students >= min_students AND max_students <= 100);

ALTER TABLE schedules
ADD CONSTRAINT chk_price_positive 
    CHECK (price_amount > 0);

-- Add index for searching by subject
CREATE INDEX idx_schedules_subject ON schedules(subject);

-- Add index for searching by session type
CREATE INDEX idx_schedules_session_type ON schedules(session_type);

-- Comments
COMMENT ON COLUMN schedules.subject IS 'Subject category (e.g., Mathematics, Physics)';
COMMENT ON COLUMN schedules.session_type IS 'Type of sessions created from this schedule';
COMMENT ON COLUMN schedules.min_students IS 'Minimum students required for GROUP sessions';
COMMENT ON COLUMN schedules.max_students IS 'Maximum students allowed per session';
COMMENT ON COLUMN schedules.price_amount IS 'Price per student in minor units';
COMMENT ON COLUMN schedules.price_currency IS 'Currency code (ISO 4217)';
