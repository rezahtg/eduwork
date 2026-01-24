package com.eduwork.identity.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for KYC (Know Your Customer) documents.
 * Represents uploaded verification documents like ID, certificates, etc.
 * 
 * Performance: Metadata stored in database, files in MinIO (S3-compatible)
 */
@Value
@Builder
public class KycDocument {
    UUID id;
    UUID userId;
    KycDocumentType type;
    String fileName; // Original filename
    String fileUrl; // MinIO URL
    long fileSizeBytes; // File size for validation
    KycStatus status;
    Instant uploadedAt;
    Instant reviewedAt; // When approved/rejected
    String reviewerNotes; // Admin notes (for rejection reason)

    /**
     * Check if document is pending review.
     */
    public boolean isPending() {
        return status == KycStatus.PENDING;
    }

    /**
     * Check if document is approved.
     */
    public boolean isApproved() {
        return status == KycStatus.APPROVED;
    }

    /**
     * Approve document.
     * Returns new instance with updated status (immutable).
     */
    public KycDocument approve(String notes) {
        return KycDocument.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .fileName(this.fileName)
                .fileUrl(this.fileUrl)
                .fileSizeBytes(this.fileSizeBytes)
                .status(KycStatus.APPROVED)
                .uploadedAt(this.uploadedAt)
                .reviewedAt(Instant.now())
                .reviewerNotes(notes)
                .build();
    }

    /**
     * Reject document.
     * Returns new instance with updated status (immutable).
     */
    public KycDocument reject(String reason) {
        return KycDocument.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .fileName(this.fileName)
                .fileUrl(this.fileUrl)
                .fileSizeBytes(this.fileSizeBytes)
                .status(KycStatus.REJECTED)
                .uploadedAt(this.uploadedAt)
                .reviewedAt(Instant.now())
                .reviewerNotes(reason)
                .build();
    }
}
