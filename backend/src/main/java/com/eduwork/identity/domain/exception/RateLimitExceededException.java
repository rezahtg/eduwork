package com.eduwork.identity.domain.exception;

/**
 * Exception thrown when an IP address exceeds the rate limit for login
 * attempts.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
