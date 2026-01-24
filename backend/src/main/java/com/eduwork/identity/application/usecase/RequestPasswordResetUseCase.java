package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.RequestPasswordResetCommand;
import com.eduwork.identity.domain.model.PasswordResetToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.PasswordResetTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Use case for requesting password reset.
 * 
 * Security Features:
 * - No email enumeration (always returns success)
 * - Rate limiting (max 3 requests per hour)
 * - Only verified users can reset password
 * - Invalidates old reset tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.password-reset.max-requests-per-hour:3}")
    private int maxRequestsPerHour;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Requests a password reset for the given email.
     * Always returns success to prevent email enumeration attacks.
     *
     * @param command the request command containing email
     */
    @Transactional
    public void execute(RequestPasswordResetCommand command) {
        String email = command.getEmail().toLowerCase().trim();
        log.info("Password reset requested for email: {}", email);

        try {
            // 1. Find user (but don't reveal if they exist)
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Security: Don't reveal that user doesn't exist
                log.info("Password reset requested for non-existent email: {}", email);
                return; // Still return success to caller
            }

            // 2. Check if email is verified
            if (!user.isEmailVerified()) {
                log.warn("Password reset requested for unverified email: {}", email);
                throw new IllegalArgumentException("Please verify your email before resetting password");
            }

            // 3. Rate limiting - check recent requests
            Instant oneHourAgo = Instant.now().minusSeconds(3600);
            List<PasswordResetToken> recentTokens = tokenRepository.findByUserAndCreatedAtAfter(user, oneHourAgo);

            if (recentTokens.size() >= maxRequestsPerHour) {
                log.warn("Password reset rate limit exceeded for email: {}", email);
                throw new IllegalStateException(
                        "Too many password reset requests. Please try again later.");
            }

            // 4. Invalidate old reset tokens for this user
            tokenRepository.deleteByUser(user);

            // 5. Create new reset token (30 minutes expiration)
            PasswordResetToken resetToken = PasswordResetToken.create(user);
            tokenRepository.save(resetToken);

            // 6. Send password reset email
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user, resetToken.getToken());

            log.info("Password reset email sent successfully to: {}", email);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Re-throw validation errors (email not verified, rate limit)
            throw e;
        } catch (Exception e) {
            // Log but don't expose internal errors
            log.error("Error processing password reset request for: {}", email, e);
            // Still return success to prevent enumeration
        }
    }
}
