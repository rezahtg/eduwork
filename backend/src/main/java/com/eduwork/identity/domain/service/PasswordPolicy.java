package com.eduwork.identity.domain.service;

import com.eduwork.identity.domain.exception.InvalidPasswordException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Domain service for password validation.
 * Enforces password policy rules.
 * 
 * Password Policy:
 * - Minimum 8 characters
 * - At least 1 uppercase letter
 * - At least 1 lowercase letter
 * - At least 1 digit
 * - At least 1 special character (@$!%*?&)
 */
@Component
public class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    /**
     * Validates password against security policy.
     * 
     * @param password raw password to validate
     * @throws InvalidPasswordException if password doesn't meet policy
     */
    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordException(
                    "Password must contain at least one uppercase letter, " +
                            "one lowercase letter, one digit, and one special character (@$!%*?&)");
        }
    }

    /**
     * Checks if password meets policy without throwing exception.
     * 
     * @param password password to check
     * @return true if valid, false otherwise
     */
    public boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (InvalidPasswordException e) {
            return false;
        }
    }
}
