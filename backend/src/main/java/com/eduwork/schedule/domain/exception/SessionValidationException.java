package com.eduwork.schedule.domain.exception;

/**
 * Exception thrown when a session validation fails.
 */
public class SessionValidationException extends RuntimeException {
    public SessionValidationException(String message) {
        super(message);
    }

    public SessionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
