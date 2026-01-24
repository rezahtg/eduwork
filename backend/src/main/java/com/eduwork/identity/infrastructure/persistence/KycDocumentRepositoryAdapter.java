package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.KycDocument;
import com.eduwork.identity.domain.model.KycDocumentType;
import com.eduwork.identity.domain.repository.KycDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA adapter implementation of KycDocumentRepository.
 */
@Component
@RequiredArgsConstructor
public class KycDocumentRepositoryAdapter implements KycDocumentRepository {

    private final KycDocumentJpaRepository jpaRepository;

    @Override
    public KycDocument save(KycDocument document) {
        KycDocumentEntity entity = toEntity(document);
        KycDocumentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<KycDocument> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<KycDocument> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KycDocument> findByUserIdAndType(UUID userId, KycDocumentType type) {
        return jpaRepository.findByUserIdAndType(userId, type).map(this::toDomain);
    }

    private KycDocumentEntity toEntity(KycDocument domain) {
        KycDocumentEntity entity = new KycDocumentEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setType(domain.getType());
        entity.setFileName(domain.getFileName());
        entity.setFileUrl(domain.getFileUrl());
        entity.setFileSizeBytes(domain.getFileSizeBytes());
        entity.setStatus(domain.getStatus());
        entity.setUploadedAt(domain.getUploadedAt());
        entity.setReviewedAt(domain.getReviewedAt());
        entity.setReviewerNotes(domain.getReviewerNotes());
        entity.setCreatedAt(domain.getUploadedAt());
        return entity;
    }

    private KycDocument toDomain(KycDocumentEntity entity) {
        return KycDocument.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .fileName(entity.getFileName())
                .fileUrl(entity.getFileUrl())
                .fileSizeBytes(entity.getFileSizeBytes())
                .status(entity.getStatus())
                .uploadedAt(entity.getUploadedAt())
                .reviewedAt(entity.getReviewedAt())
                .reviewerNotes(entity.getReviewerNotes())
                .build();
    }
}
