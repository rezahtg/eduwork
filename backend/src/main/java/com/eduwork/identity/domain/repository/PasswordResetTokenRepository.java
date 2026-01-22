package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.PasswordResetToken;
import com.eduwork.identity.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for PasswordResetToken.
 * To be implemented by infrastructure layer.
 */
public interface PasswordResetTokenRepository {

    /**
     * Save a password reset token.
     *
     * @param token the token to save
     * @return the saved token
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Find a token by its value.
     *
     * @param token the token value
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find recent reset tokens for a user (for rate limiting).
     *
     * @param user  the user
     * @param since timestamp to search from
     * @return list of tokens created after the given timestamp
     */
    List<PasswordResetToken> findByUserAndCreatedAtAfter(User user, Instant since);

    /**
     * Delete all expired tokens.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    int deleteExpiredTokens(Instant now);

    /**
     * Delete all tokens for a specific user.
     * Used to invalidate old tokens when new reset is requested.
     *
     * @param user the user
     */
    void deleteByUser(User user);
}
