package com.eduwork.booking.application.command;

import com.eduwork.booking.domain.model.CancellationReason;

import java.util.UUID;

/**
 * Command to cancel a booking.
 */
public record CancelBookingCommand(
        UUID bookingId,
        UUID studentId, // For authorization check
        String reason // Optional user-provided reason
) {
    public CancelBookingCommand {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
    }

    /**
     * Get cancellation reason enum.
     * Defaults to STUDENT_REQUEST if no specific reason provided.
     */
    public CancellationReason getCancellationReason() {
        return CancellationReason.STUDENT_REQUEST;
    }
}
