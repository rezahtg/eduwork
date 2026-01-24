-- V013: Add KYC Documents Table
-- Purpose: Store KYC document metadata for user verification

CREATE TABLE kyc_documents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    reviewer_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_kyc_user_type UNIQUE(user_id, type)
);

-- Indexes for performance
CREATE INDEX idx_kyc_user_id ON kyc_documents(user_id);
CREATE INDEX idx_kyc_status ON kyc_documents(status) WHERE status = 'PENDING';
CREATE INDEX idx_kyc_type ON kyc_documents(type);

-- Comments for documentation
COMMENT ON TABLE kyc_documents IS 'KYC (Know Your Customer) document uploads for user verification';
COMMENT ON COLUMN kyc_documents.type IS 'Document type: ID_CARD, PASSPORT, STUDENT_CARD, CERTIFICATE, DEGREE';
COMMENT ON COLUMN kyc_documents.status IS 'Verification status: PENDING, APPROVED, REJECTED';
COMMENT ON COLUMN kyc_documents.file_url IS 'MinIO object storage URL';
COMMENT ON COLUMN kyc_documents.reviewer_notes IS 'Admin notes for approval/rejection reason';
