package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.KycDocument;
import com.eduwork.identity.domain.repository.KycDocumentRepository;
import com.eduwork.identity.presentation.dto.KycDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: Get KYC documents for user
 * 
 * Performance: Simple database read, expected < 50ms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetKycDocumentsUseCase {

    private final KycDocumentRepository documentRepository;

    /**
     * Get all KYC documents for a user.
     * 
     * @param userId user ID
     * @return list of documents
     */
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> execute(UUID userId) {
        log.debug("Fetching KYC documents for user: {}", userId);

        List<KycDocument> documents = documentRepository.findByUserId(userId);

        log.debug("Found {} KYC documents for user: {}", documents.size(), userId);

        return documents.stream()
                .map(KycDocumentResponse::from)
                .collect(Collectors.toList());
    }
}
