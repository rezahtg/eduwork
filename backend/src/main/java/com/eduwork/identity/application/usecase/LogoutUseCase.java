package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.repository.RefreshTokenRepository;
import com.eduwork.identity.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for user logout.
 * Revokes the refresh token to prevent future token refreshes.
 * 
 * Note: Access tokens cannot be revoked (they expire naturally in 15 minutes).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Logs out user by revoking refresh token.
     *
     * @param refreshTokenJwt the refresh token JWT to revoke
     */
    @Transactional
    public void execute(String refreshTokenJwt) {
        log.info("Logout request received");

        try {
            // 1. Validate JWT and extract token ID
            Claims claims = jwtService.validateRefreshToken(refreshTokenJwt);
            String tokenId = jwtService.extractJti(claims);

            // 2. Find and revoke token
            RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenId)
                    .orElseThrow(() -> {
                        log.warn("Refresh token not found for logout: {}", tokenId);
                        return new IllegalArgumentException("Invalid refresh token");
                    });

            // 3. Revoke token (idempotent)
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);

            log.info("Logout successful, refresh token revoked");

        } catch (JwtException e) {
            log.warn("Invalid JWT during logout: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}
