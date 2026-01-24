package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.LoginCommand;
import com.eduwork.identity.application.dto.AuthenticationResponse;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.application.mapper.UserMapper;
import com.eduwork.identity.domain.exception.AccountLockedException;
import com.eduwork.identity.domain.exception.RateLimitExceededException;
import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.model.UserSession;
import com.eduwork.identity.domain.repository.RefreshTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.domain.repository.UserSessionRepository;
import com.eduwork.identity.domain.service.BruteForceProtectionService;
import com.eduwork.identity.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Use case for user login.
 * 
 * Business Rules:
 * - User must exist
 * - Email must be verified before login
 * - Password must match
 * - Account must be ACTIVE
 * - Generates access token (15 min) + refresh token (7 days)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtection;

    /**
     * Authenticates user and returns tokens.
     *
     * @param command   login credentials
     * @param ipAddress client IP address for brute force protection
     * @return authentication response with tokens
     * @throws AccountLockedException     if account is locked
     * @throws RateLimitExceededException if IP is blocked
     * @throws IllegalArgumentException   if credentials invalid or email not
     *                                    verified
     */
    @Transactional
    public AuthenticationResponse execute(LoginCommand command, String ipAddress) {
        String email = command.getEmail().toLowerCase();
        log.info("Login attempt for email: {} from IP: {}", email, ipAddress);

        // 1. Check if IP is blocked
        if (bruteForceProtection.isIpBlocked(ipAddress)) {
            log.warn("Login blocked - IP rate limit exceeded: {}", ipAddress);
            throw new RateLimitExceededException(
                    "Too many login attempts from your IP address. Please try again later.");
        }

        // 2. Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    bruteForceProtection.recordFailedAttempt(email, ipAddress);
                    log.warn("Login failed: user not found: {}", email);
                    return new IllegalArgumentException("Invalid email or password");
                });

        // 3. Check if account is locked (database-based)
        if (user.isLocked()) {
            log.warn("Login blocked - account locked in database: {}", email);
            throw new AccountLockedException(
                    "Account is locked until " + user.getLockedUntil() +
                            ". Please try again later or contact support.");
        }

        // 4. Check if account is locked (Redis-based)
        if (bruteForceProtection.isAccountLocked(email)) {
            // Sync lock to database
            user.lock(Duration.ofMinutes(30));
            userRepository.save(user);

            log.warn("Login blocked - account locked due to failed attempts: {}", email);
            throw new AccountLockedException(
                    "Account is temporarily locked due to multiple failed login attempts. " +
                            "Please try again in 30 minutes or contact support.");
        }

        // 5. Verify email is verified
        if (!user.isEmailVerified()) {
            log.warn("Login failed: email not verified: {}", email);
            throw new IllegalArgumentException("Please verify your email before logging in");
        }

        // 6. Verify password
        if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
            bruteForceProtection.recordFailedAttempt(email, ipAddress);
            log.warn("Login failed: invalid password for user: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 7. Check account status is ACTIVE
        if (!user.isActive()) {
            log.warn("Login failed: account not active: {}", email);
            throw new IllegalArgumentException("Account is not active. Please contact support.");
        }

        // 8. SUCCESS - Reset failed attempts
        bruteForceProtection.recordSuccessfulLogin(email, ipAddress);

        // 9. Create session
        UUID sessionId = UUID.randomUUID();
        UserSession session = UserSession.builder()
                .id(sessionId)
                .userId(user.getId())
                .deviceInfo(command.getDeviceInfo() != null ? command.getDeviceInfo() : "Unknown")
                .ipAddress(ipAddress)
                .createdAt(Instant.now())
                .lastAccessedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(7))) // 7 days session
                .build();
        sessionRepository.save(session);

        // 10. Generate access token
        String accessToken = jwtService.generateAccessToken(user);

        // 11. Generate refresh token
        String tokenId = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.create(user, tokenId);
        refreshTokenRepository.save(refreshToken);

        // 12. Generate refresh token JWT
        String refreshTokenJwt = jwtService.generateRefreshTokenJwt(user.getId(), tokenId);

        log.info("Login successful for user: {}, session: {}", user.getEmail(), sessionId);

        // 13. Return response
        UserResponseDTO userDto = UserMapper.toResponseDTO(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenJwt)
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .sessionId(sessionId)
                .user(userDto)
                .build();
    }
}
