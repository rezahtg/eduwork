package com.eduwork.identity.domain.exception;

/**
 * Thrown when password does not meet security policy requirements.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
