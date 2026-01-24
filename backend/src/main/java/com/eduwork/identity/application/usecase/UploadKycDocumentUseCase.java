package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.KycDocument;
import com.eduwork.identity.domain.model.KycDocumentType;
import com.eduwork.identity.domain.model.KycStatus;
import com.eduwork.identity.domain.repository.KycDocumentRepository;
import com.eduwork.identity.infrastructure.storage.FileStorageService;
import com.eduwork.identity.presentation.dto.KycDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Use case: Upload KYC document
 * 
 * Async processing: File upload is I/O intensive, run async
 * Performance: Doesn't block request thread
 * 
 * Business Rules:
 * - File size max 10MB
 * - Allowed types: JPEG, PNG, PDF
 * - One document per type per user (replace if exists)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadKycDocumentUseCase {

    private final KycDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_TYPES = { "image/jpeg", "image/png", "application/pdf" };

    /**
     * Upload KYC document asynchronously.
     * 
     * @param userId user ID
     * @param type   document type
     * @param file   uploaded file
     * @return future with document response
     */
    @Async
    @Transactional
    public CompletableFuture<KycDocumentResponse> execute(
            UUID userId,
            KycDocumentType type,
            MultipartFile file) {

        log.info("Uploading KYC document for user {}: type={}, file={}",
                userId, type, file.getOriginalFilename());

        // 1. Validate file
        validateFile(file);

        // 2. Store file in MinIO
        String fileUrl = fileStorageService.store(file, userId, "kyc");

        // 3. Create domain model
        KycDocument document = KycDocument.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileSizeBytes(file.getSize())
                .status(KycStatus.PENDING)
                .uploadedAt(Instant.now())
                .reviewedAt(null)
                .reviewerNotes(null)
                .build();

        // 4. Save metadata to database
        KycDocument saved = documentRepository.save(document);

        log.info("KYC document uploaded successfully: id={}", saved.getId());

        return CompletableFuture.completedFuture(KycDocumentResponse.from(saved));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        String contentType = file.getContentType();
        boolean isAllowedType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                isAllowedType = true;
                break;
            }
        }

        if (!isAllowedType) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed: JPEG, PNG, PDF");
        }
    }
}
