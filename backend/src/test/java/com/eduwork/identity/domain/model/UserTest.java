package com.eduwork.identity.domain.model;

import com.eduwork.identity.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for User domain entity.
 */
@DisplayName("User Domain Entity")
class UserTest {

    @Test
    @DisplayName("Should create user with valid data")
    void shouldCreateUser_whenValidData() {
        // Given
        String email = "student@example.com";
        String passwordHash = "$2a$12$hashvalue";
        String phone = "+6281234567890";

        // When
        User user = new User(email, passwordHash, phone);

        // Then
        assertNotNull(user.getId());
        assertEquals("student@example.com", user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(phone, user.getPhone());
        assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
        assertFalse(user.isProfileComplete());
        assertNull(user.getEmailVerifiedAt());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmail_toLowerCase() {
        // Given
        String mixedCaseEmail = "Student@Example.COM";

        // When
        User user = new User(mixedCaseEmail, "hash", null);

        // Then
        assertEquals("student@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowException_whenEmailIsNull() {
        // When & Then
        InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> new User(null, "hash", null));
        assertEquals("Invalid email format: Email cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email is empty")
    void shouldThrowException_whenEmailIsEmpty() {
        // When & Then
        assertThrows(
                InvalidEmailException.class,
                () -> new User("", "hash", null));
    }

    @Test
    @DisplayName("Should throw exception when email format invalid")
    void shouldThrowException_whenEmailFormatInvalid() {
        // Given
        String[] invalidEmails = {
                "notanemail",
                "@example.com",
                "user@",
                "user @example.com",
                "user@example",
                "user..test@example.com"
        };

        // When & Then
        for (String invalidEmail : invalidEmails) {
            assertThrows(
                    InvalidEmailException.class,
                    () -> new User(invalidEmail, "hash", null),
                    "Should reject: " + invalidEmail);
        }
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAccept_whenEmailFormatValid() {
        // Given
        String[] validEmails = {
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.id",
                "user_123@test-domain.com"
        };

        // When & Then
        for (String validEmail : validEmails) {
            assertDoesNotThrow(
                    () -> new User(validEmail, "hash", null),
                    "Should accept: " + validEmail);
        }
    }

    @Test
    @DisplayName("Should verify email and activate user")
    void shouldVerifyEmail_andActivateUser() {
        // Given
        User user = new User("test@example.com", "hash", null);

        // When
        user.verifyEmail();

        // Then
        assertTrue(user.isEmailVerified());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getEmailVerifiedAt());
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Should complete profile")
    void shouldCompleteProfile() {
        // Given
        User user = new User("test@example.com", "hash", null);
        assertFalse(user.isProfileComplete());

        // When
        user.completeProfile();

        // Then
        assertTrue(user.isProfileComplete());
    }

    @Test
    @DisplayName("Should suspend user")
    void shouldSuspendUser() {
        // Given
        User user = new User("test@example.com", "hash", null);
        user.verifyEmail(); // Make active first

        // When
        user.suspend();

        // Then
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        assertFalse(user.isActive());
    }

    @Test
    @DisplayName("Should reactivate suspended user")
    void shouldReactivate_whenSuspended() {
        // Given
        User user = new User("test@example.com", "hash", null);
        user.verifyEmail();
        user.suspend();

        // When
        user.activate();

        // Then
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Should soft delete user")
    void shouldSoftDelete() {
        // Given
        User user = new User("test@example.com", "hash", null);

        // When
        user.delete();

        // Then
        assertEquals(UserStatus.DELETED, user.getStatus());
        assertFalse(user.isActive());
    }

    @Test
    @DisplayName("Should create user without phone")
    void shouldCreateUser_whenPhoneIsNull() {
        // Given
        String email = "test@example.com";

        // When
        User user = new User(email, "hash", null);

        // Then
        assertNotNull(user);
        assertNull(user.getPhone());
    }

    @Test
    @DisplayName("Should reconstitute user from database")
    void shouldReconstitute_fromDatabase() {
        // Given
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        Instant now = Instant.now();

        // When
        User user = new User(
                id, email, null, "hash",
                UserStatus.ACTIVE, true,
                now, null,
                null, // lockedUntil
                null, null, now, now);

        // Then
        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.isProfileComplete());
        assertEquals(now, user.getEmailVerifiedAt());
    }

    @Test
    @DisplayName("Should trim whitespace from email")
    void shouldTrimWhitespace_fromEmail() {
        // Given
        String emailWithSpaces = "  test@example.com  ";

        // When
        User user = new User(emailWithSpaces, "hash", null);

        // Then
        assertEquals("test@example.com", user.getEmail());
    }
}
