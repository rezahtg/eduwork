package com.eduwork.booking.presentation.dto;

import java.util.UUID;

/**
 * Response DTO for successful booking.
 */
public record BookSessionResponse(
        UUID bookingId,
        String message) {
    public static BookSessionResponse success(UUID bookingId) {
        return new BookSessionResponse(
                bookingId,
                "Session booked successfully. Please complete payment within 24 hours.");
    }
}
