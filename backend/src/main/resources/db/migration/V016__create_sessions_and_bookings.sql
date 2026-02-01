-- V016__create_sessions_and_bookings.sql
-- Redesign Schedule & Booking Module with proper domain modeling
-- Author: Eduwork Platform Team
-- Date: 2026-01-25

-- ============================================================
-- Part 1: Update schedules table with new fields
-- ============================================================

-- Drop old schedule_time_slots table (replaced by sessions table)
DROP TABLE IF EXISTS schedule_time_slots CASCADE;

-- Update schedules table
ALTER TABLE schedules
    -- Remove old type column
    DROP COLUMN IF EXISTS type CASCADE,
    DROP CONSTRAINT IF EXISTS schedules_status_check CASCADE,
    
    -- Add new fields
    ADD COLUMN IF NOT EXISTS session_type VARCHAR(20) NOT NULL DEFAULT 'ONE_ON_ONE'
        CHECK (session_type IN ('ONE_ON_ONE', 'GROUP')),
    ADD COLUMN IF NOT EXISTS subject VARCHAR(100),
    ADD COLUMN IF NOT EXISTS tags TEXT[],
    ADD COLUMN IF NOT EXISTS price_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    ADD COLUMN IF NOT EXISTS min_students INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS max_students INT NOT NULL DEFAULT 1;

-- Add constraints
ALTER TABLE schedules
    ADD CONSTRAINT check_schedule_capacity CHECK (max_students >= min_students),
    ADD CONSTRAINT check_max_group_size CHECK (max_students <= 100),
    ADD CONSTRAINT check_price_amount CHECK (price_amount >= 0),
    ADD CONSTRAINT schedules_status_check 
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_schedules_subject ON schedules(subject);
CREATE INDEX IF NOT EXISTS idx_schedules_session_type ON schedules(session_type);
CREATE INDEX IF NOT EXISTS idx_schedules_price ON schedules(price_amount);

-- ============================================================
-- Part 2: Create sessions table (bookable instances)
-- ============================================================

CREATE TABLE sessions (
    -- Primary Key
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    
    -- Time
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    
    -- Configuration (denormalized from schedule for query performance)
    session_type VARCHAR(20) NOT NULL CHECK (session_type IN ('ONE_ON_ONE', 'GROUP')),
    price_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    min_students INT NOT NULL,
    max_students INT NOT NULL,
    
    -- Current State
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'OPEN', 'WAITING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'REVIEWED', 'CANCELLED')),
    current_enrollment INT NOT NULL DEFAULT 0,
    
    -- Optimistic Locking (prevents overbooking on last slot)
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_session_time_range CHECK (end_time > start_time),
    CONSTRAINT check_session_capacity CHECK (max_students >= min_students),
    CONSTRAINT check_session_enrollment CHECK (current_enrollment >= 0 AND current_enrollment <= max_students),
    CONSTRAINT check_price CHECK (price_amount >= 0)
);

-- Indexes for performance
CREATE INDEX idx_sessions_schedule_id ON sessions(schedule_id);
CREATE INDEX idx_sessions_status ON sessions(status);
CREATE INDEX idx_sessions_start_time ON sessions(start_time);
CREATE INDEX idx_sessions_start_status ON sessions(start_time, status) WHERE status IN ('OPEN', 'WAITING', 'CONFIRMED');
CREATE INDEX idx_sessions_type ON sessions(session_type);

-- ============================================================
-- Part 3: Create bookings table (student enrollments)
-- ============================================================

CREATE TABLE bookings (
    -- Primary Key
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Financial (platform fee model: 15% platform, 85% mentor)
    amount_paid DECIMAL(12,2) NOT NULL,
    platform_fee DECIMAL(12,2) NOT NULL,     -- 15% of amount_paid
    mentor_payout DECIMAL(12,2) NOT NULL,    -- 85% of amount_paid
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    
    -- Payment
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'IN_SESSION', 'COMPLETED', 'CANCELLED', 'REFUNDED')
    ),
    payment_deadline TIMESTAMP NOT NULL,
    payment_reference VARCHAR(255),  -- External payment gateway reference
    
    -- Refund
    refund_amount DECIMAL(12,2),
    refunded_at TIMESTAMP,
    cancellation_reason VARCHAR(50) CHECK (cancellation_reason IN ('STUDENT_REQUEST', 'MENTOR_CANCELLED', 'PAYMENT_TIMEOUT', 'MINIMUM_NOT_MET', 'EMERGENCY', 'ADMIN_ACTION')),
    
    -- Timestamps
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- Constraints
    UNIQUE(session_id, student_id),  -- One booking per student per session
    CONSTRAINT check_booking_amount CHECK (amount_paid > 0),
    CONSTRAINT check_platform_fee CHECK (platform_fee >= 0 AND platform_fee <= amount_paid),
    CONSTRAINT check_mentor_payout CHECK (mentor_payout >= 0 AND mentor_payout <= amount_paid),
    CONSTRAINT check_refund_amount CHECK (refund_amount IS NULL OR (refund_amount >= 0 AND refund_amount <= amount_paid))
);

-- Indexes for performance
CREATE INDEX idx_bookings_session_id ON bookings(session_id);
CREATE INDEX idx_bookings_student_id ON bookings(student_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_payment_deadline ON bookings(payment_deadline) WHERE status = 'PENDING_PAYMENT';
CREATE INDEX idx_bookings_session_student ON bookings(session_id, student_id);

-- ============================================================
-- Part 4: Triggers and Functions
-- ============================================================

-- Trigger to update sessions.updated_at
CREATE OR REPLACE FUNCTION update_sessions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sessions_updated_at
    BEFORE UPDATE ON sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_sessions_updated_at();

-- ============================================================
-- Part 5: Comments for documentation
-- ============================================================

COMMENT ON TABLE schedules IS 'Schedule templates created by mentors (redesigned with session_type, pricing, capacity)';
COMMENT ON TABLE sessions IS 'Bookable session instances created from schedule templates';
COMMENT ON TABLE bookings IS 'Student enrollments in sessions with platform fee tracking';

COMMENT ON COLUMN schedules.session_type IS 'ONE_ON_ONE (1 student) or GROUP (2-100 students)';
COMMENT ON COLUMN schedules.price_amount IS 'Price per student in smallest currency unit';
COMMENT ON COLUMN schedules.min_students IS 'Minimum students required to proceed (GROUP sessions)';
COMMENT ON COLUMN schedules.max_students IS 'Maximum students allowed (1 for ONE_ON_ONE, 2-100 for GROUP)';

COMMENT ON COLUMN sessions.version IS 'Optimistic lock version for preventing race conditions on concurrent bookings';
COMMENT ON COLUMN sessions.current_enrollment IS 'Current number of active bookings (incremented/decremented on book/cancel)';
COMMENT ON COLUMN sessions.status IS 'Session lifecycle: DRAFT→OPEN→WAITING/CONFIRMED→IN_PROGRESS→COMPLETED→REVIEWED';

COMMENT ON COLUMN bookings.platform_fee IS 'Eduwork platform fee (15% of amount_paid, non-refundable)';
COMMENT ON COLUMN bookings.mentor_payout IS 'Amount paid to mentor (85% of amount_paid)';
COMMENT ON COLUMN bookings.payment_deadline IS 'Deadline for payment (typically 24 hours after booking)';
COMMENT ON COLUMN bookings.refund_amount IS 'Calculated refund amount based on cancellation timing and policy';
COMMENT ON COLUMN bookings.status IS 'Booking lifecycle: PENDING_PAYMENT→PAID→CONFIRMED→IN_SESSION→COMPLETED or CANCELLED→REFUNDED';

-- ============================================================
-- Part 6: Migration data (if needed)
-- ============================================================

-- Update existing schedules to have default values
UPDATE schedules 
SET 
    session_type = 'ONE_ON_ONE',
    min_students = 1,
    max_students = 1,
    price_amount = 0,
    subject = 'General'
WHERE session_type IS NULL;

-- ============================================================
-- End of migration
-- ============================================================
