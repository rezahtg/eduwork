package com.eduwork.identity.domain.service;

import com.eduwork.identity.domain.model.User;

/**
 * Email service port (interface).
 * Implementation will be in infrastructure layer.
 */
public interface EmailService {

    /**
     * Sends email verification link to user.
     * This method should be async to avoid blocking registration.
     * 
     * @param user              user who needs verification
     * @param verificationToken token for verification link
     */
    void sendVerificationEmail(User user, String verificationToken);

    /**
     * Sends password reset email.
     * 
     * @param user       user requesting reset
     * @param resetToken token for reset link
     */
    void sendPasswordResetEmail(User user, String resetToken);
}
