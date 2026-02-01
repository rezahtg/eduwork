-- V015: Recreate schedules table for new schedule module design
-- Purpose: Replace old schedule schema with new design including timezone support

-- Drop old tables (cascade will drop dependencies)
DROP TABLE IF EXISTS attendances CASCADE;
DROP TABLE IF EXISTS live_sessions CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS schedules CASCADE;

-- Create new schedules table
CREATE TABLE schedules (
    id UUID PRIMARY KEY,
    mentor_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL CHECK (type IN ('ONE_TIME', 'RECURRING')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED')),
    timezone VARCHAR(100) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    
    CONSTRAINT fk_schedules_mentor FOREIGN KEY (mentor_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Create time slots table
CREATE TABLE schedule_time_slots (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('AVAILABLE', 'BOOKED', 'CANCELLED')),
    booking_id UUID,
    booked_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    CONSTRAINT fk_time_slots_schedule FOREIGN KEY (schedule_id) 
        REFERENCES schedules(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_time_slot_duration CHECK (end_time > start_time)
);

-- Indexes for performance
CREATE INDEX idx_schedules_mentor_id ON schedules(mentor_id);
CREATE INDEX idx_schedules_status ON schedules(status);
CREATE INDEX idx_schedules_type ON schedules(type);
CREATE INDEX idx_schedules_published_at ON schedules(published_at) WHERE published_at IS NOT NULL;
CREATE INDEX idx_time_slots_schedule_id ON schedule_time_slots(schedule_id);
CREATE INDEX idx_time_slots_status ON schedule_time_slots(status);
CREATE INDEX idx_time_slots_start_time ON schedule_time_slots(start_time);
CREATE INDEX idx_time_slots_booking_id ON schedule_time_slots(booking_id) WHERE booking_id IS NOT NULL;

-- Composite index for searching published schedules by date range
CREATE INDEX idx_schedules_published_search 
    ON schedules(status, published_at) 
    WHERE status = 'PUBLISHED';

-- Update trigger
CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE schedules IS 'Mentor availability schedules with timezone support (redesigned in V015)';
COMMENT ON TABLE schedule_time_slots IS 'Individual time slots within schedules';
COMMENT ON COLUMN schedules.timezone IS 'Timezone ID (e.g., Asia/Jakarta) for calendar integration';
COMMENT ON COLUMN schedules.type IS 'ONE_TIME for individual sessions, RECURRING for repeating schedules';
COMMENT ON COLUMN schedules.status IS 'DRAFT (editing), PUBLISHED (visible to students), CANCELLED';
COMMENT ON COLUMN schedule_time_slots.status IS 'AVAILABLE, BOOKED, or CANCELLED';
