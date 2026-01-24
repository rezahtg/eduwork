package com.eduwork.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for PasswordResetToken domain entity.
 */
@DisplayName("PasswordResetToken Domain Entity")
class PasswordResetTokenTest {

    private User createTestUser() {
        return new User(
                "test@example.com",
                "$2a$12$hashedPassword",
                null);
    }

    @Test
    @DisplayName("Should create valid password reset token with 30-minute expiration")
    void shouldCreateValidToken() {
        // Given
        User user = createTestUser();
        Instant beforeCreation = Instant.now();

        // When
        PasswordResetToken token = PasswordResetToken.create(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getId()).isNotNull();
        assertThat(token.getToken()).isNotNull().isNotEmpty();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(token.getUsedAt()).isNull();

        // Verify 30-minute expiration (1800000 milliseconds)
        long expirationDuration = token.getExpiresAt().toEpochMilli() - token.getCreatedAt().toEpochMilli();
        assertThat(expirationDuration).isEqualTo(1800000L);
    }

    @Test
    @DisplayName("Should throw exception when creating token with null user")
    void shouldThrowException_whenUserIsNull() {
        // When & Then
        assertThatThrownBy(() -> PasswordResetToken.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastExpiration = Instant.now().minusSeconds(1);
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                "token-123",
                user,
                pastExpiration,
                Instant.now().minusSeconds(1800),
                null);

        // When & Then
        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should detect non-expired token")
    void shouldDetectNonExpiredToken() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);

        // When & Then
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should mark token as used successfully")
    void shouldMarkTokenAsUsed() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);
        Instant beforeUsage = Instant.now();

        // When
        token.markAsUsed();

        // Then
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isAfterOrEqualTo(beforeUsage);
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should be idempotent when marking already used token")
    void shouldBeIdempotent_whenMarkingUsedToken() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);
        token.markAsUsed();
        Instant firstUsage = token.getUsedAt();

        // When - mark as used again
        token.markAsUsed();

        // Then - usage timestamp should not change
        assertThat(token.getUsedAt()).isEqualTo(firstUsage);
    }

    @Test
    @DisplayName("Should detect used token as invalid")
    void shouldDetectUsedTokenAsInvalid() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);

        // When
        token.markAsUsed();

        // Then
        assertThat(token.isUsed()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should validate successfully for valid token")
    void shouldValidateSuccessfullyForValidToken() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);

        // When & Then - should not throw
        assertThatCode(() -> token.validate()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when validating expired token")
    void shouldThrowException_whenValidatingExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastExpiration = Instant.now().minusSeconds(1);
        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                "token-123",
                user,
                pastExpiration,
                Instant.now().minusSeconds(1800),
                null);

        // When & Then
        assertThatThrownBy(() -> token.validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should throw exception when validating used token")
    void shouldThrowException_whenValidatingUsedToken() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);
        token.markAsUsed();

        // When & Then
        assertThatThrownBy(() -> token.validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    @DisplayName("Should be valid when not expired and not used")
    void shouldBeValid_whenNotExpiredAndNotUsed() {
        // Given
        User user = createTestUser();
        PasswordResetToken token = PasswordResetToken.create(user);

        // When & Then
        assertThat(token.isValid()).isTrue();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    @DisplayName("Should generate unique tokens for different users")
    void shouldGenerateUniqueTokens() {
        // Given
        User user1 = createTestUser();
        User user2 = new User("another@example.com", "$2a$12$hash", null);

        // When
        PasswordResetToken token1 = PasswordResetToken.create(user1);
        PasswordResetToken token2 = PasswordResetToken.create(user2);

        // Then
        assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
        assertThat(token1.getId()).isNotEqualTo(token2.getId());
    }

    @Test
    @DisplayName("Should have correct expiration time of 30 minutes")
    void shouldHaveCorrectExpirationTime() {
        // Given
        User user = createTestUser();

        // When
        PasswordResetToken token = PasswordResetToken.create(user);

        // Then
        long expectedExpiration = 30 * 60 * 1000; // 30 minutes in milliseconds
        long actualDuration = token.getExpiresAt().toEpochMilli() - token.getCreatedAt().toEpochMilli();
        assertThat(actualDuration).isEqualTo(expectedExpiration);
    }
}
