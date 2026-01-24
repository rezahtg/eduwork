package com.eduwork.identity.presentation.dto;

import com.eduwork.identity.domain.model.KycDocument;
import com.eduwork.identity.domain.model.KycDocumentType;
import com.eduwork.identity.domain.model.KycStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for KYC document.
 */
@Value
@Builder
public class KycDocumentResponse {
    UUID id;
    KycDocumentType type;
    String fileName;
    String fileUrl;
    long fileSizeBytes;
    KycStatus status;
    Instant uploadedAt;
    Instant reviewedAt;
    String reviewerNotes;

    /**
     * Factory method to create response from domain KycDocument.
     */
    public static KycDocumentResponse from(KycDocument document) {
        return KycDocumentResponse.builder()
                .id(document.getId())
                .type(document.getType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileSizeBytes(document.getFileSizeBytes())
                .status(document.getStatus())
                .uploadedAt(document.getUploadedAt())
                .reviewedAt(document.getReviewedAt())
                .reviewerNotes(document.getReviewerNotes())
                .build();
    }
}
