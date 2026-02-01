package com.eduwork.booking.domain.exception;

import java.util.UUID;

/**
 * Thrown when attempting to book a session that the student has already booked.
 */
public class DuplicateBookingException extends RuntimeException {
    private final UUID sessionId;
    private final UUID studentId;

    public DuplicateBookingException(UUID sessionId, UUID studentId) {
        super(String.format("Student %s has already booked session %s", studentId, sessionId));
        this.sessionId = sessionId;
        this.studentId = studentId;
    }

    public DuplicateBookingException(String message) {
        super(message);
        this.sessionId = null;
        this.studentId = null;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getStudentId() {
        return studentId;
    }
}
