package com.eduwork.identity.presentation.controller;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.usecase.GetKycDocumentsUseCase;
import com.eduwork.identity.application.usecase.UploadKycDocumentUseCase;
import com.eduwork.identity.domain.model.KycDocumentType;
import com.eduwork.identity.presentation.dto.KycDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for KYC document management.
 * Handles document upload and status retrieval.
 */
@Slf4j
@RestController
@RequestMapping("/users/me/kyc")
@RequiredArgsConstructor
public class KycController {

    private final UploadKycDocumentUseCase uploadKycDocumentUseCase;
    private final GetKycDocumentsUseCase getKycDocumentsUseCase;

    /**
     * Upload KYC document.
     * Async processing - returns immediately after upload starts.
     * 
     * @param userId user ID from JWT
     * @param type   document type
     * @param file   uploaded file
     * @return async response with document details
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<ApiResponse<KycDocumentResponse>>> uploadDocument(
            @AuthenticationPrincipal String userId,
            @RequestParam("type") KycDocumentType type,
            @RequestParam("file") MultipartFile file) {

        log.info("KYC upload request from user {}: type={}", userId, type);

        return uploadKycDocumentUseCase.execute(UUID.fromString(userId), type, file)
                .thenApply(document -> ResponseEntity.ok(
                        ApiResponse.success(document, "Document uploaded successfully. Pending review.")));
    }

    /**
     * Get all KYC documents for current user.
     * 
     * @param userId user ID from JWT
     * @return list of documents
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getDocuments(
            @AuthenticationPrincipal String userId) {

        log.info("Fetching KYC documents for user: {}", userId);

        List<KycDocumentResponse> documents = getKycDocumentsUseCase.execute(
                UUID.fromString(userId));

        return ResponseEntity.ok(
                ApiResponse.success(documents,
                        String.format("Found %d document(s)", documents.size())));
    }
}
