package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.model.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Domain repository interface for RefreshToken.
 * To be implemented by infrastructure layer.
 */
public interface RefreshTokenRepository {

    /**
     * Save a refresh token.
     *
     * @param token the token to save
     * @return the saved token
     */
    RefreshToken save(RefreshToken token);

    /**
     * Find a token by its JTI (JWT ID).
     *
     * @param tokenId the JWT ID
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByToken(String tokenId);

    /**
     * Find active (non-revoked, non-expired) token for a user.
     *
     * @param user the user
     * @return Optional containing active token if found
     */
    Optional<RefreshToken> findActiveTokenByUser(User user);

    /**
     * Delete all expired tokens.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    int deleteExpiredTokens(Instant now);

    /**
     * Revoke all tokens for a specific user.
     *
     * @param user the user
     */
    void revokeAllByUser(User user);
}
