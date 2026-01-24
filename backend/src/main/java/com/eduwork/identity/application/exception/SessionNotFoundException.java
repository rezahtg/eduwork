package com.eduwork.identity.application.exception;

import java.util.UUID;

/**
 * Exception thrown when a session is not found.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(UUID sessionId) {
        super(String.format("Session not found with ID: %s", sessionId));
    }
}
