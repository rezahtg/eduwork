package com.eduwork.identity.domain.exception;

/**
 * Thrown when email format is invalid.
 */
public class InvalidEmailException extends RuntimeException {

    public InvalidEmailException(String email) {
        super("Invalid email format: " + email);
    }
}
