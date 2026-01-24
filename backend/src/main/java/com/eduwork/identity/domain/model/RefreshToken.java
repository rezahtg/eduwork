package com.eduwork.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a refresh token for JWT authentication.
 * Refresh tokens are used to obtain new access tokens without
 * re-authentication.
 * 
 * Business Rules:
 * - Token expires after 7 days
 * - Token can only be used once (single-use)
 * - Revoked tokens cannot be used
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class RefreshToken {

    private UUID id;
    private String token; // JTI - JWT ID (unique identifier)
    private User user;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant revokedAt;

    /**
     * Public constructor for infrastructure layer.
     * Used by persistence mappers to reconstruct from database.
     */
    public RefreshToken(UUID id, String token, User user, Instant expiresAt, Instant createdAt, Instant revokedAt) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
    }

    /**
     * Creates a new refresh token for the given user.
     * Token expires in 7 days (604800000 milliseconds).
     *
     * @param user    the user this token belongs to
     * @param tokenId the unique JWT ID (JTI)
     * @return new refresh token
     */
    public static RefreshToken create(User user, String tokenId) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (tokenId == null || tokenId.trim().isEmpty()) {
            throw new IllegalArgumentException("Token ID cannot be null or empty");
        }

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(604800000L); // 7 days

        RefreshToken token = new RefreshToken();
        token.id = UUID.randomUUID();
        token.token = tokenId;
        token.user = user;
        token.expiresAt = expiration;
        token.createdAt = now;
        token.revokedAt = null;

        return token;
    }

    /**
     * Checks if the token has expired.
     *
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token has been revoked.
     *
     * @return true if token is revoked, false otherwise
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Checks if the token is valid (not expired and not revoked).
     *
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Revokes the token, preventing its future use.
     * Idempotent operation - can be called multiple times.
     */
    public void revoke() {
        if (revokedAt == null) {
            this.revokedAt = Instant.now();
        }
    }

    /**
     * Validates the token and throws exception if invalid.
     *
     * @throws IllegalStateException if token is expired or revoked
     */
    public void validate() {
        if (isExpired()) {
            throw new IllegalStateException("Refresh token has expired");
        }
        if (isRevoked()) {
            throw new IllegalStateException("Refresh token has been revoked");
        }
    }
}
