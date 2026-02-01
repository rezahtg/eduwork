package com.eduwork.booking.application.command;

import java.util.UUID;

/**
 * Command to book a session.
 * Contains all necessary data for creating a booking.
 */
public record BookSessionCommand(
        UUID sessionId,
        UUID studentId) {
    public BookSessionCommand {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
    }
}
