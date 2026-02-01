package com.eduwork.schedule.domain.exception;

import java.util.UUID;

/**
 * Thrown when attempting to book a session that is already full.
 */
public class SessionFullException extends RuntimeException {
    private final UUID sessionId;

    public SessionFullException(UUID sessionId) {
        super(String.format("Session %s is full and cannot accept more bookings", sessionId));
        this.sessionId = sessionId;
    }

    public SessionFullException(String message) {
        super(message);
        this.sessionId = null;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
