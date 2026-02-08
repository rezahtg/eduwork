package com.eduwork.schedule.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a session is not found.
 */
public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String message) {
        super(message);
    }

    public SessionNotFoundException(UUID sessionId) {
        super("Session not found with ID: " + sessionId);
    }
}
