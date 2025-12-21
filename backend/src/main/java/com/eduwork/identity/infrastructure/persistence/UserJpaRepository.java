package com.eduwork.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserEntity.
 * Provides database operations.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds user by email (case-insensitive).
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    /**
     * Checks if email exists (case-insensitive).
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if phone exists.
     */
    boolean existsByPhone(String phone);
}
