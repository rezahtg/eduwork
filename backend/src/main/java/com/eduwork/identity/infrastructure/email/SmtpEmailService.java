package com.eduwork.identity.infrastructure.email;

import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using Spring Mail (SMTP).
 * Sends emails asynchronously using @Async.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@eduwork.com}")
    private String fromEmail;

    @Value("${app.mail.verification-url:http://localhost:8080/api/v1/auth/verify-email}")
    private String verificationBaseUrl;

    @Override
    @Async
    public void sendVerificationEmail(User user, String verificationToken) {
        try {
            log.info("Sending verification email to: {}", user.getEmail());

            String verificationUrl = verificationBaseUrl + "?token=" + verificationToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify Your Eduwork Account");
            message.setText(buildVerificationEmailBody(verificationUrl));

            mailSender.send(message);

            log.info("Verification email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            // In production, consider: retry queue, dead letter queue, monitoring
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            log.info("Sending password reset email to: {}", user.getEmail());

            String resetUrl = "http://localhost:8080/api/v1/auth/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Reset Your Eduwork Password");
            message.setText(buildPasswordResetEmailBody(resetUrl));

            mailSender.send(message);

            log.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    private String buildVerificationEmailBody(String verificationUrl) {
        return """
                Welcome to Eduwork!

                Please verify your email address by clicking the link below:

                %s

                This link will expire in 24 hours.

                If you didn't create an account, please ignore this email.

                Best regards,
                The Eduwork Team
                """.formatted(verificationUrl);
    }

    private String buildPasswordResetEmailBody(String resetUrl) {
        return """
                Password Reset Request

                We received a request to reset your password. Click the link below to reset it:

                %s

                This link will expire in 1 hour.

                If you didn't request this, please ignore this email.

                Best regards,
                The Eduwork Team
                """.formatted(resetUrl);
    }
}
