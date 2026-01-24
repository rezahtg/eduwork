package com.eduwork.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EmailVerificationToken Domain Tests")
class EmailVerificationTokenTest {

    @Test
    @DisplayName("Should create valid token with correct expiration time")
    void shouldCreateValidToken() {
        // Given
        User user = createTestUser();

        // When
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getId()).isNotNull();
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getCreatedAt()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(token.getCreatedAt());
        assertThat(token.getUsedAt()).isNull();

        // Token should expire 15 minutes after creation
        long expirationDuration = token.getExpiresAt().toEpochMilli() - token.getCreatedAt().toEpochMilli();
        assertThat(expirationDuration).isEqualTo(900000L); // 15 minutes in ms
    }

    @Test
    @DisplayName("Should throw exception when creating token with null user")
    void shouldThrowExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> EmailVerificationToken.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User cannot be null");
    }

    @Test
    @DisplayName("Should identify expired token correctly")
    void shouldIdentifyExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastTime = Instant.now().minusSeconds(1000);
        EmailVerificationToken expiredToken = createTokenWithExpiration(user, pastTime);

        // Then
        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(expiredToken.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should identify non-expired token correctly")
    void shouldIdentifyNonExpiredToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // Then
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should mark token as used successfully")
    void shouldMarkTokenAsUsed() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // When
        token.markAsUsed();

        // Then
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when marking already used token")
    void shouldThrowExceptionWhenMarkingUsedToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);
        token.markAsUsed();

        // When & Then
        assertThatThrownBy(() -> token.markAsUsed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token has already been used");
    }

    @Test
    @DisplayName("Should throw exception when marking expired token as used")
    void shouldThrowExceptionWhenMarkingExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastTime = Instant.now().minusSeconds(1000);
        EmailVerificationToken expiredToken = createTokenWithExpiration(user, pastTime);

        // When & Then
        assertThatThrownBy(() -> expiredToken.markAsUsed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot use expired token");
    }

    @Test
    @DisplayName("Should identify used token correctly")
    void shouldIdentifyUsedToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // When
        token.markAsUsed();

        // Then
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("Should identify unused token correctly")
    void shouldIdentifyUnusedToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // Then
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    @DisplayName("Should validate token correctly - valid token")
    void shouldValidateValidToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);

        // Then
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should validate token correctly - expired token is invalid")
    void shouldInvalidateExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastTime = Instant.now().minusSeconds(1000);
        EmailVerificationToken expiredToken = createTokenWithExpiration(user, pastTime);

        // Then
        assertThat(expiredToken.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should validate token correctly - used token is invalid")
    void shouldInvalidateUsedToken() {
        // Given
        User user = createTestUser();
        EmailVerificationToken token = EmailVerificationToken.create(user);
        token.markAsUsed();

        // Then
        assertThat(token.isValid()).isFalse();
    }

    // Helper methods
    private User createTestUser() {
        return new User(
                "test@example.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz", // Mock BCrypt hash
                "081234567890");
    }

    private EmailVerificationToken createTokenWithExpiration(User user, Instant expiresAt) {
        // Create a test token using reflection since constructor is private
        EmailVerificationToken token = EmailVerificationToken.create(user);
        try {
            java.lang.reflect.Field expiresAtField = EmailVerificationToken.class.getDeclaredField("expiresAt");
            expiresAtField.setAccessible(true);
            expiresAtField.set(token, expiresAt);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test token", e);
        }
    }
}
