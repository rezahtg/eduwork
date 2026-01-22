package com.eduwork.identity.infrastructure.email;

import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

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

    @Override
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "Welcome to Eduwork!";
        String body = String.format(
                "Hi %s,\n\n" +
                        "Welcome to Eduwork! We're excited to have you on board.\n\n" +
                        "Best regards,\n" +
                        "The Eduwork Team",
                fullName);

        send(to, subject, body);
    }

    @Override
    public void sendVerificationEmail(String to, String fullName, String verificationLink) {
        String subject = "Verify Your Eduwork Account";
        String body = String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                                .content { padding: 20px; background-color: #f9f9f9; }
                                .button {
                                    display: inline-block;
                                    padding: 12px 24px;
                                    background-color: #4CAF50;
                                    color: white !important;
                                    text-decoration: none;
                                    border-radius: 4px;
                                    margin: 20px 0;
                                }
                                .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                                .warning { color: #f44336; font-size: 14px; margin-top: 15px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Welcome to Eduwork!</h1>
                                </div>
                                <div class="content">
                                    <h2>Hi %s,</h2>
                                    <p>Thank you for signing up! Please verify your email address to activate your account.</p>
                                    <p>Click the button below to verify your email:</p>
                                    <p style="text-align: center;">
                                        <a href="%s" class="button">Verify Email Address</a>
                                    </p>
                                    <p>Or copy and paste this link into your browser:</p>
                                    <p style="word-break: break-all; background-color: #fff; padding: 10px; border: 1px solid #ddd;">%s</p>
                                    <p class="warning">⚠️ This link will expire in 15 minutes.</p>
                                    <p>If you didn't create an account with Eduwork, please ignore this email.</p>
                                </div>
                                <div class="footer">
                                    <p>&copy; 2024 Eduwork. All rights reserved.</p>
                                    <p>This is an automated email. Please do not reply.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                fullName, verificationLink, verificationLink);

        sendHtml(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Plain text email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send plain text email to: {}", to, e);
        }
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
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

    @Override
    @Async
    public void sendPasswordResetConfirmationEmail(String to, String fullName) {
        try {
            log.info("Sending password reset confirmation email to: {}", to);

            String subject = "Password Changed Successfully - Eduwork";
            String body = buildPasswordResetConfirmationEmailBody(fullName);

            sendHtml(to, subject, body);

            log.info("Password reset confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", to, e);
        }
    }

    private String buildPasswordResetConfirmationEmailBody(String fullName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #4F46E5; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Changed</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Your password for Eduwork was successfully changed.</p>
                            <p><strong>If you made this change, you can safely ignore this email.</strong></p>
                            <p>If you did NOT make this change, please contact our support team immediately, as your account may have been compromised.</p>
                            <p style="margin-top: 30px;">
                                <a href="${FRONTEND_URL}/login" class="button">Login Now</a>
                            </p>
                        </div>
                        <div class="footer">
                            <p>If you have any questions, contact us at support@eduwork.com</p>
                            <p>&copy; 2025 Eduwork Platform. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(fullName);
    }
}
