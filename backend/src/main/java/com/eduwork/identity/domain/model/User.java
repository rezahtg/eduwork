package com.eduwork.identity.domain.model;

import com.eduwork.identity.domain.exception.InvalidEmailException;
import jakarta.persistence.Embedded;
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
    private Instant lockedUntil; // NEW: For brute force protection
    private Instant createdAt;
    private Instant updatedAt;

    // Role-specific profiles (embedded)
    @Embedded
    private StudentProfile studentProfile;

    @Embedded
    private MentorProfile mentorProfile;

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
            Instant lockedUntil,
            StudentProfile studentProfile, MentorProfile mentorProfile,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = status;
        this.profileComplete = profileComplete;
        this.emailVerifiedAt = emailVerifiedAt;
        this.phoneVerifiedAt = phoneVerifiedAt;
        this.lockedUntil = lockedUntil;
        this.studentProfile = studentProfile;
        this.mentorProfile = mentorProfile;
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
     * Updates user password.
     * Used when resetting forgotten password.
     *
     * @param newPasswordHash the new hashed password
     */
    public void updatePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
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

    /**
     * Completes student profile.
     * Sets profileComplete flag to true.
     *
     * @param profile student profile data
     */
    public void completeStudentProfile(StudentProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Student profile cannot be null");
        }
        this.studentProfile = profile;
        this.profileComplete = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Completes mentor profile.
     * Sets profileComplete flag to true.
     *
     * @param profile mentor profile data
     */
    public void completeMentorProfile(MentorProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Mentor profile cannot be null");
        }
        this.mentorProfile = profile;
        this.profileComplete = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates existing student profile.
     *
     * @param profile updated student profile data
     */
    public void updateStudentProfile(StudentProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Student profile cannot be null");
        }
        this.studentProfile = profile;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates existing mentor profile.
     *
     * @param profile updated mentor profile data
     */
    public void updateMentorProfile(MentorProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Mentor profile cannot be null");
        }
        this.mentorProfile = profile;
        this.updatedAt = Instant.now();
    }

    /**
     * Locks the account for a specified duration.
     * Used for brute force protection.
     *
     * @param duration how long the account should remain locked
     */
    public void lock(java.time.Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Lock duration must be positive");
        }
        this.lockedUntil = Instant.now().plus(duration);
        this.updatedAt = Instant.now();
    }

    /**
     * Unlocks the account immediately.
     */
    public void unlock() {
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the account is currently locked.
     * Auto-unlocks if the lock duration has expired.
     *
     * @return true if account is locked, false otherwise
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }

        // Check if lock has expired
        if (Instant.now().isAfter(lockedUntil)) {
            this.lockedUntil = null; // Auto-unlock
            return false;
        }

        return true;
    }
}
