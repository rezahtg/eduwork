-- V002__create_schedules_and_bookings_tables.sql
-- Scheduling and Booking Modules

-- Schedules table
CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_per_session DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'IDR',
    session_type VARCHAR(20) NOT NULL CHECK (session_type IN ('PRIVATE', 'GROUP')),
    max_students INT DEFAULT 1,
    min_students INT DEFAULT 1,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    recurrence_rule VARCHAR(100),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'FULL', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    waiting_extended_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_time_range CHECK (end_time > start_time),
    CONSTRAINT check_student_count CHECK (max_students >= min_students),
    CONSTRAINT check_price CHECK (price_per_session >= 0)
);

CREATE INDEX idx_schedules_mentor_id ON schedules(mentor_id);
CREATE INDEX idx_schedules_subject ON schedules(subject);
CREATE INDEX idx_schedules_status ON schedules(status);
CREATE INDEX idx_schedules_start_time ON schedules(start_time);
CREATE INDEX idx_schedules_session_type ON schedules(session_type);

-- Live sessions
CREATE TABLE live_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID UNIQUE NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    platform VARCHAR(20) NOT NULL CHECK (platform IN ('GOOGLE_MEET', 'ZOOM')),
    meeting_url VARCHAR(500),
    meeting_id VARCHAR(100),
    meeting_password VARCHAR(50),
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_live_sessions_schedule_id ON live_sessions(schedule_id);

-- Bookings table
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_type VARCHAR(20) NOT NULL CHECK (booking_type IN ('PRIVATE', 'GROUP')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'IN_SESSION', 'COMPLETED', 'DISPUTED')),
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    completed_by VARCHAR(20) CHECK (completed_by IN ('STUDENT', 'MENTOR', 'SYSTEM', 'ADMIN')),
    UNIQUE(schedule_id, student_id)
);

CREATE INDEX idx_bookings_schedule_id ON bookings(schedule_id);
CREATE INDEX idx_bookings_student_id ON bookings(student_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_booked_at ON bookings(booked_at);

-- Attendances
CREATE TABLE attendances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'MENTOR')),
    check_in_at TIMESTAMP,
    check_out_at TIMESTAMP,
    duration_minutes INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendances_booking_id ON attendances(booking_id);
CREATE INDEX idx_attendances_user_id ON attendances(user_id);

-- Add update trigger
CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
