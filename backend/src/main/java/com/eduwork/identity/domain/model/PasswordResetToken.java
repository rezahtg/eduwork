package com.eduwork.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a password reset token.
 * Reset tokens allow users to securely reset their forgotten passwords via
 * email.
 * 
 * Business Rules:
 * - Token expires after 30 minutes (more time than email verification due to
 * critical nature)
 * - Token can only be used once (single-use)
 * - User must have verified email to request password reset
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class PasswordResetToken {

    private UUID id;
    private String token; // Unique cryptographically secure token
    private User user;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant usedAt;

    /**
     * Public constructor for infrastructure layer.
     * Used by persistence mappers to reconstruct from database.
     */
    public PasswordResetToken(UUID id, String token, User user, Instant expiresAt, Instant createdAt, Instant usedAt) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.usedAt = usedAt;
    }

    /**
     * Creates a new password reset token for the given user.
     * Token expires in 30 minutes (1800000 milliseconds).
     *
     * @param user the user requesting password reset
     * @return new password reset token
     * @throws IllegalArgumentException if user is null
     */
    public static PasswordResetToken create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(1800000L); // 30 minutes
        String tokenValue = UUID.randomUUID().toString();

        PasswordResetToken token = new PasswordResetToken();
        token.id = UUID.randomUUID();
        token.token = tokenValue;
        token.user = user;
        token.expiresAt = expiration;
        token.createdAt = now;
        token.usedAt = null;

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
     * Checks if the token has been used.
     *
     * @return true if token has been used, false otherwise
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Checks if the token is valid (not expired and not used).
     *
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Marks the token as used.
     * This is called when the user successfully resets their password.
     * Idempotent operation - can be called multiple times safely.
     */
    public void markAsUsed() {
        if (usedAt == null) {
            this.usedAt = Instant.now();
        }
    }

    /**
     * Validates the token and throws exception if invalid.
     *
     * @throws IllegalStateException if token is expired or already used
     */
    public void validate() {
        if (isExpired()) {
            throw new IllegalStateException("Password reset token has expired");
        }
        if (isUsed()) {
            throw new IllegalStateException("Password reset token has already been used");
        }
    }
}
