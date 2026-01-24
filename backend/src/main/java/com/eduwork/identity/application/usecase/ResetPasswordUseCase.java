package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.command.ResetPasswordCommand;
import com.eduwork.identity.domain.model.PasswordResetToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.PasswordResetTokenRepository;
import com.eduwork.identity.domain.repository.UserRepository;
import com.eduwork.identity.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for resetting password with token.
 * 
 * Business Rules:
 * - Token must be valid (not expired, not used)
 * - New password must meet policy requirements
 * - Token is marked as used after successful reset
 * - Confirmation email sent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Resets user password using the provided token.
     *
     * @param command the reset command containing token and new password
     * @throws IllegalArgumentException if token is invalid or password doesn't meet
     *                                  requirements
     */
    @Transactional
    public void execute(ResetPasswordCommand command) {
        log.info("Password reset attempt with token");

        // 1. Find and validate token
        PasswordResetToken resetToken = tokenRepository.findByToken(command.getToken())
                .orElseThrow(() -> {
                    log.warn("Invalid password reset token provided");
                    return new IllegalArgumentException("Invalid password reset link");
                });

        // 2. Validate token (not expired, not used)
        resetToken.validate();

        // 3. Get user
        User user = resetToken.getUser();

        // 4. Validate new password
        validatePassword(command.getNewPassword());

        // 5. Mark token as used
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        // 6. Update user password
        String hashedPassword = passwordEncoder.encode(command.getNewPassword());
        user.updatePassword(hashedPassword);
        userRepository.save(user);

        // 7. Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getEmail());

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /**
     * Validates password meets security requirements.
     * 
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}
