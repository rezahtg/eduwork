-- V003__create_discussion_and_payment_tables.sql
-- Discussion and Payment Modules

-- Pre-booking discussions
CREATE TABLE pre_booking_discussions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'RESOLVED', 'BOOKED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    UNIQUE(schedule_id, student_id)
);

CREATE INDEX idx_pre_discussions_schedule_id ON pre_booking_discussions(schedule_id);
CREATE INDEX idx_pre_discussions_student_id ON pre_booking_discussions(student_id);

-- Booking discussions
CREATE TABLE booking_discussions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID UNIQUE NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_discussions_booking_id ON booking_discussions(booking_id);

-- Messages (shared for both discussion types)
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    discussion_type VARCHAR(30) NOT NULL CHECK (discussion_type IN ('PRE_BOOKING', 'BOOKING')),
    discussion_id UUID NOT NULL,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_messages_discussion ON messages(discussion_type, discussion_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);

-- Transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID UNIQUE NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    amount DECIMAL(12,2) NOT NULL,
    platform_fee DECIMAL(12,2),
    mentor_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'IDR',
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('MANUAL_TRANSFER', 'MIDTRANS', 'XENDIT', 'DOKU')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'PROOF_SUBMITTED', 'VERIFIED', 'HELD', 'RELEASED', 'REFUNDED')),
    external_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_amount CHECK (amount >= 0)
);

CREATE INDEX idx_transactions_booking_id ON transactions(booking_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Payment proofs (for manual transfer)
CREATE TABLE payment_proofs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    proof_url VARCHAR(500) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    rejection_reason TEXT
);

CREATE INDEX idx_payment_proofs_transaction_id ON payment_proofs(transaction_id);

-- Disputes
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    raised_by UUID NOT NULL REFERENCES users(id),
    reason VARCHAR(50) NOT NULL CHECK (reason IN ('SESSION_NOT_CONDUCTED', 'QUALITY_ISSUE', 'OTHER')),
    description TEXT NOT NULL,
    evidence_urls TEXT[],
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_disputes_transaction_id ON disputes(transaction_id);
CREATE INDEX idx_disputes_status ON disputes(status);

-- Dispute resolutions
CREATE TABLE dispute_resolutions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id UUID UNIQUE NOT NULL REFERENCES disputes(id) ON DELETE CASCADE,
    resolved_by UUID NOT NULL REFERENCES users(id),
    resolution_type VARCHAR(30) NOT NULL CHECK (resolution_type IN ('REFUND_FULL', 'REFUND_PARTIAL', 'RELEASE_TO_MENTOR', 'SPLIT')),
    refund_amount DECIMAL(12,2),
    mentor_amount DECIMAL(12,2),
    notes TEXT,
    resolved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dispute_resolutions_dispute_id ON dispute_resolutions(dispute_id);

-- Add update triggers
CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_disputes_updated_at BEFORE UPDATE ON disputes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
