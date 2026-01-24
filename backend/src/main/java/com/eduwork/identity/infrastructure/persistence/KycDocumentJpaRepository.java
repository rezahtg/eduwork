package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.KycDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for KYC documents.
 */
public interface KycDocumentJpaRepository extends JpaRepository<KycDocumentEntity, UUID> {

    List<KycDocumentEntity> findByUserId(UUID userId);

    Optional<KycDocumentEntity> findByUserIdAndType(UUID userId, KycDocumentType type);
}
