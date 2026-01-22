package com.eduwork.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RefreshToken domain entity.
 */
@DisplayName("RefreshToken Domain Entity")
class RefreshTokenTest {

    private User createTestUser() {
        return new User(
                "test@example.com",
                "$2a$12$hashedPassword",
                null);
    }

    @Test
    @DisplayName("Should create valid refresh token with 7-day expiration")
    void shouldCreateValidToken() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        Instant beforeCreation = Instant.now();

        // When
        RefreshToken token = RefreshToken.create(user, tokenId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getId()).isNotNull();
        assertThat(token.getToken()).isEqualTo(tokenId);
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(token.getRevokedAt()).isNull();

        // Verify 7-day expiration (604800000 milliseconds)
        long expirationDuration = token.getExpiresAt().toEpochMilli() - token.getCreatedAt().toEpochMilli();
        assertThat(expirationDuration).isEqualTo(604800000L);
    }

    @Test
    @DisplayName("Should throw exception when creating token with null user")
    void shouldThrowException_whenUserIsNull() {
        // Given
        String tokenId = UUID.randomUUID().toString();

        // When & Then
        assertThatThrownBy(() -> RefreshToken.create(null, tokenId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when creating token with null token ID")
    void shouldThrowException_whenTokenIdIsNull() {
        // Given
        User user = createTestUser();

        // When & Then
        assertThatThrownBy(() -> RefreshToken.create(user, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when creating token with empty token ID")
    void shouldThrowException_whenTokenIdIsEmpty() {
        // Given
        User user = createTestUser();

        // When & Then
        assertThatThrownBy(() -> RefreshToken.create(user, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastExpiration = Instant.now().minusSeconds(1);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                "token-123",
                user,
                pastExpiration,
                Instant.now().minusSeconds(604800),
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
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);

        // When & Then
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should revoke token successfully")
    void shouldRevokeToken() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);
        Instant beforeRevocation = Instant.now();

        // When
        token.revoke();

        // Then
        assertThat(token.isRevoked()).isTrue();
        assertThat(token.getRevokedAt()).isAfterOrEqualTo(beforeRevocation);
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should be idempotent when revoking already revoked token")
    void shouldBeIdempotent_whenRevokingRevokedToken() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);
        token.revoke();
        Instant firstRevocation = token.getRevokedAt();

        // When - revoke again
        token.revoke();

        // Then - revocation timestamp should not change
        assertThat(token.getRevokedAt()).isEqualTo(firstRevocation);
    }

    @Test
    @DisplayName("Should detect revoked token as invalid")
    void shouldDetectRevokedTokenAsInvalid() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);

        // When
        token.revoke();

        // Then
        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should validate successful for valid token")
    void shouldValidateSuccessfullyForValidToken() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);

        // When & Then - should not throw
        assertThatCode(() -> token.validate()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when validating expired token")
    void shouldThrowException_whenValidatingExpiredToken() {
        // Given
        User user = createTestUser();
        Instant pastExpiration = Instant.now().minusSeconds(1);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                "token-123",
                user,
                pastExpiration,
                Instant.now().minusSeconds(604800),
                null);

        // When & Then
        assertThatThrownBy(() -> token.validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should throw exception when validating revoked token")
    void shouldThrowException_whenValidatingRevokedToken() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);
        token.revoke();

        // When & Then
        assertThatThrownBy(() -> token.validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    @DisplayName("Should be valid when not expired and not revoked")
    void shouldBeValid_whenNotExpiredAndNotRevoked() {
        // Given
        User user = createTestUser();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.create(user, tokenId);

        // When & Then
        assertThat(token.isValid()).isTrue();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isRevoked()).isFalse();
    }
}
