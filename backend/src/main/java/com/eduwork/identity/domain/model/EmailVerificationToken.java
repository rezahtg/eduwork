package com.eduwork.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Email verification token for confirming user email addresses.
 * Tokens expire after 15 minutes and are single-use only.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class EmailVerificationToken {

    private UUID id;
    private String token;
    private User user;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant usedAt;

    /**
     * Public constructor for infrastructure layer.
     * Used by persistence mappers to reconstruct from database.
     */
    public EmailVerificationToken(UUID id, String token, User user, Instant expiresAt, Instant createdAt,
            Instant usedAt) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.usedAt = usedAt;
    }

    /**
     * Creates a new verification token for the given user.
     * Token expires after 15 minutes from creation.
     *
     * @param user The user this token is for
     * @return A new verification token
     */
    public static EmailVerificationToken create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(900000); // 15 minutes

        return new EmailVerificationToken(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                user,
                expiration,
                now,
                null);
    }

    /**
     * Checks if this token has expired.
     *
     * @return true if the token has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if this token has already been used.
     *
     * @return true if the token has been used
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Marks this token as used.
     *
     * @throws IllegalStateException if token is already used
     */
    public void markAsUsed() {
        if (isUsed()) {
            throw new IllegalStateException("Token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot use expired token");
        }
        this.usedAt = Instant.now();
    }

    /**
     * Validates if this token can be used for verification.
     *
     * @return true if token is valid (not expired, not used)
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }
}
