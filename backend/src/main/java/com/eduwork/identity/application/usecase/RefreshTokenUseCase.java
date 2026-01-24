package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.dto.AuthenticationResponse;
import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.RefreshTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for refreshing access token using refresh token.
 * 
 * Business Rules:
 * - Refresh token must be valid JWT
 * - Token must exist in database
 * - Token must not be expired
 * - Token must not be revoked
 * - Old refresh token is revoked
 * - New tokens are generated
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Refreshes access token using refresh token.
     *
     * @param refreshTokenJwt the refresh token JWT string
     * @return new authentication response with new tokens
     * @throws IllegalArgumentException if token invalid, expired, or revoked
     */
    @Transactional
    public AuthenticationResponse execute(String refreshTokenJwt) {
        log.info("Refresh token request received");

        try {
            // 1. Validate JWT signature and expiration
            Claims claims = jwtService.validateRefreshToken(refreshTokenJwt);
            String tokenId = jwtService.extractJti(claims);
            UUID userId = jwtService.extractUserId(claims);

            // 2. Find token in database
            RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenId)
                    .orElseThrow(() -> {
                        log.warn("Refresh token not found in database: {}", tokenId);
                        return new IllegalArgumentException("Invalid refresh token");
                    });

            // 3. Validate token (not expired, not revoked)
            refreshToken.validate();

            // 4. Get user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found for refresh token: {}", userId);
                        return new IllegalStateException("User not found");
                    });

            // 5. Revoke old refresh token
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);

            // 6. Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);

            // 7. Generate new refresh token
            String newTokenId = UUID.randomUUID().toString();
            RefreshToken newRefreshToken = RefreshToken.create(user, newTokenId);
            refreshTokenRepository.save(newRefreshToken);

            // 8. Generate new refresh token JWT
            String newRefreshTokenJwt = jwtService.generateRefreshTokenJwt(user.getId(), newTokenId);

            log.info("Tokens refreshed successfully for user: {}", user.getEmail());

            // 9. Return new tokens (no user info needed for refresh)
            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshTokenJwt)
                    .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                    .build();

        } catch (JwtException e) {
            log.warn("Invalid JWT refresh token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
    }
}
