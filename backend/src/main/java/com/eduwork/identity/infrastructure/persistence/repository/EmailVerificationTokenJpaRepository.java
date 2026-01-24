package com.eduwork.identity.infrastructure.persistence.repository;

import com.eduwork.identity.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.eduwork.identity.infrastructure.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for EmailVerificationToken.
 */
@Repository
public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {

    /**
     * Find token by token string.
     *
     * @param token the token string
     * @return Optional containing the token entity if found
     */
    Optional<EmailVerificationTokenEntity> findByToken(String token);

    /**
     * Find all tokens for a user created after a specific time.
     * Used for rate limiting resend requests.
     *
     * @param user  the user entity
     * @param since the timestamp to search from
     * @return list of tokens
     */
    List<EmailVerificationTokenEntity> findByUserAndCreatedAtAfter(UserEntity user, Instant since);

    /**
     * Delete all expired tokens.
     * Should be called periodically by a scheduled job.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all tokens for a specific user.
     * Used when user successfully verifies email.
     *
     * @param user the user entity
     */
    void deleteByUser(UserEntity user);
}
