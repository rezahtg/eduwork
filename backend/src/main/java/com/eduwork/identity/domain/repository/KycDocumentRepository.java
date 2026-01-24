package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.KycDocument;
import com.eduwork.identity.domain.model.KycDocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for KYC document management.
 */
public interface KycDocumentRepository {

    /**
     * Save KYC document.
     * 
     * @param document document to save
     * @return saved document
     */
    KycDocument save(KycDocument document);

    /**
     * Find document by ID.
     * 
     * @param id document ID
     * @return document if found
     */
    Optional<KycDocument> findById(UUID id);

    /**
     * Find all documents for a user.
     * 
     * @param userId user ID
     * @return list of documents
     */
    List<KycDocument> findByUserId(UUID userId);

    /**
     * Find document by user and type.
     * Used to check if user already uploaded a specific document type.
     * 
     * @param userId user ID
     * @param type   document type
     * @return document if found
     */
    Optional<KycDocument> findByUserIdAndType(UUID userId, KycDocumentType type);
}
