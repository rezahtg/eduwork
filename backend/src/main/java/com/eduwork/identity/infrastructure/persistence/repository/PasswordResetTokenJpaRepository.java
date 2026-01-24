package com.eduwork.identity.infrastructure.persistence.repository;

import com.eduwork.identity.infrastructure.persistence.UserEntity;
import com.eduwork.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for PasswordResetTokenEntity.
 */
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    /**
     * Find password reset token by token string.
     *
     * @param token the token value
     * @return Optional containing the token entity
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Find recent tokens for a user (for rate limiting).
     *
     * @param user  the user entity
     * @param since timestamp to search from
     * @return list of tokens created after the given timestamp
     */
    List<PasswordResetTokenEntity> findByUserAndCreatedAtAfter(UserEntity user, Instant since);

    /**
     * Delete all expired tokens.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity prt WHERE prt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all tokens for a specific user.
     *
     * @param user the user entity
     */
    void deleteByUser(UserEntity user);
}
