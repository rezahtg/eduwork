package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.EmailVerificationToken;
import com.eduwork.identity.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for EmailVerificationToken.
 * To be implemented by infrastructure layer.
 */
public interface EmailVerificationTokenRepository {

    /**
     * Save a verification token.
     *
     * @param token the token to save
     * @return the saved token
     */
    EmailVerificationToken save(EmailVerificationToken token);

    /**
     * Find a token by its token string.
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Find all tokens for a user created after a specific time.
     * Used for rate limiting resend requests.
     *
     * @param user  the user
     * @param since the timestamp to search from
     * @return list of tokens
     */
    List<EmailVerificationToken> findByUserAndCreatedAtAfter(User user, Instant since);

    /**
     * Delete all expired tokens.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    int deleteExpiredTokens(Instant now);

    /**
     * Delete all tokens for a specific user.
     *
     * @param user the user
     */
    void deleteByUser(User user);
}
