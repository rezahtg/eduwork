package com.eduwork.identity.infrastructure.persistence.repository;

import com.eduwork.identity.infrastructure.persistence.UserEntity;
import com.eduwork.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for RefreshTokenEntity.
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /**
     * Find refresh token by token string (JTI).
     *
     * @param token the JWT ID
     * @return Optional containing the token entity
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Find active (non-revoked, non-expired) token for a user.
     *
     * @param user the user entity
     * @param now  current timestamp
     * @return Optional containing active token
     */
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.user = :user " +
            "AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    Optional<RefreshTokenEntity> findActiveTokenByUser(@Param("user") UserEntity user, @Param("now") Instant now);

    /**
     * Delete all expired tokens.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Revoke all tokens for a specific user.
     *
     * @param user the user entity
     * @param now  current timestamp for revocation
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revokedAt = :now " +
            "WHERE rt.user = :user AND rt.revokedAt IS NULL")
    void revokeAllByUser(@Param("user") UserEntity user, @Param("now") Instant now);
}
