package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.EmailVerificationToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.EmailVerificationTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for verifying user email address.
 * Business rules:
 * - Token must exist and be valid (not expired, not used)
 * - User must not be already verified
 * - Token is marked as used after successful verification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyEmailUseCase {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Verify user email using token.
     *
     * @param token the verification token string
     * @throws IllegalArgumentException if token is invalid or expired
     * @throws IllegalStateException    if user is already verified
     */
    @Transactional
    public void execute(String token) {
        log.info("Attempting to verify email with token: {}", token);

        // Find token
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        // Validate token
        if (!verificationToken.isValid()) {
            if (verificationToken.isExpired()) {
                log.warn("Verification token expired: {}", token);
                throw new IllegalArgumentException("Verification token has expired. Please request a new one.");
            }
            if (verificationToken.isUsed()) {
                log.warn("Verification token already used: {}", token);
                throw new IllegalArgumentException("Verification token has already been used");
            }
        }

        User user = verificationToken.getUser();

        // Check if already verified
        if (user.isEmailVerified()) {
            log.warn("User already verified: {}", user.getEmail());
            throw new IllegalStateException("Email address is already verified");
        }

        // Mark token as used
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        // Verify user email
        user.verifyEmail();
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }
}
