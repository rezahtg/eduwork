package com.eduwork.identity.domain.model;

import com.eduwork.identity.domain.exception.InvalidEmailException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * User aggregate root.
 * Represents a user account in the Eduwork platform.
 * 
 * Domain invariants:
 * - Email must be valid and unique
 * - Email stored in lowercase
 * - Password must meet security policy (enforced externally)
 * - New users start as PENDING_VERIFICATION
 * - Profile is incomplete by default
 */
@Getter
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]([A-Za-z0-9+_.-]*[A-Za-z0-9])?@[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}$");

    private UUID id;
    private String email;
    private String phone;
    private String passwordHash;
    private UserStatus status;
    private boolean profileComplete;
    private Instant emailVerifiedAt;
    private Instant phoneVerifiedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructor for creating new user (registration).
     * Encapsulates business rules for new user creation.
     * 
     * @param email        user email (will be normalized to lowercase)
     * @param passwordHash BCrypt hashed password
     * @param phone        optional phone number
     * @throws InvalidEmailException if email format invalid
     */
    public User(String email, String passwordHash, String phone) {
        this.id = UUID.randomUUID();
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.profileComplete = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Constructor for reconstituting from database.
     * Used by infrastructure layer to rebuild domain entity.
     */
    public User(UUID id, String email, String phone, String passwordHash,
            UserStatus status, boolean profileComplete,
            Instant emailVerifiedAt, Instant phoneVerifiedAt,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = status;
        this.profileComplete = profileComplete;
        this.emailVerifiedAt = emailVerifiedAt;
        this.phoneVerifiedAt = phoneVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Normalizes and validates email.
     * 
     * @param email raw email
     * @return lowercase email
     * @throws InvalidEmailException if email invalid
     */
    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be empty");
        }

        String normalized = email.trim().toLowerCase();

        // Check for consecutive dots (not allowed in email local part)
        if (normalized.contains("..")) {
            throw new InvalidEmailException(normalized);
        }

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException(normalized);
        }

        return normalized;
    }

    /**
     * Marks email as verified.
     * Business rule: Email verification activates account.
     */
    public void verifyEmail() {
        this.emailVerifiedAt = Instant.now();
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks profile as complete.
     */
    public void completeProfile() {
        this.profileComplete = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Suspends user account.
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivates suspended account.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Soft deletes account.
     * Business rule: We keep data for auditing, just mark as deleted.
     */
    public void delete() {
        this.status = UserStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if email is verified.
     */
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    /**
     * Checks if account is active.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
