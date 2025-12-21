package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.EmailVerificationToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.EmailVerificationTokenRepository;
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
 * Use case for resending verification email.
 * Business rules:
 * - User must exist and not be verified
 * - Rate limiting: Max 3 emails per hour
 * - Old unused tokens for this user are deleted
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResendVerificationEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.verification.max-resend-attempts:3}")
    private int maxResendAttempts;

    @Value("${app.verification.resend-cooldown:3600000}") // 1 hour in ms
    private long resendCooldown;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Resend verification email to user.
     *
     * @param email user's email address
     * @throws IllegalArgumentException if user not found or already verified
     * @throws IllegalStateException    if rate limit exceeded
     */
    @Transactional
    public void execute(String email) {
        log.info("Resending verification email to: {}", email);

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if already verified
        if (user.isEmailVerified()) {
            log.warn("Attempt to resend verification email to already verified user: {}", email);
            throw new IllegalArgumentException("Email address is already verified");
        }

        // Rate limiting check
        Instant oneHourAgo = Instant.now().minusMillis(resendCooldown);
        List<EmailVerificationToken> recentTokens = tokenRepository.findByUserAndCreatedAtAfter(user, oneHourAgo);

        if (recentTokens.size() >= maxResendAttempts) {
            log.warn("Rate limit exceeded for user: {}. Attempts: {}", email, recentTokens.size());
            throw new IllegalStateException(
                    String.format("Too many verification emails sent. Please try again later. " +
                            "Maximum %d emails per hour allowed.", maxResendAttempts));
        }

        // Create new token
        EmailVerificationToken token = EmailVerificationToken.create(user);
        tokenRepository.save(token);

        // Send email
        String verificationLink = frontendUrl + "/verify-email?token=" + token.getToken();
        emailService.sendVerificationEmail(user.getEmail(), "User", verificationLink);

        log.info("Verification email resent successfully to: {}", email);
    }
}
