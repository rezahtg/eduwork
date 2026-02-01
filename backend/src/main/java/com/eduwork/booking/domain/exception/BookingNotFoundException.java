package com.eduwork.booking.domain.exception;

import java.util.UUID;

/**
 * Thrown when a booking is not found.
 */
public class BookingNotFoundException extends RuntimeException {
    private final UUID bookingId;

    public BookingNotFoundException(UUID bookingId) {
        super(String.format("Booking not found: %s", bookingId));
        this.bookingId = bookingId;
    }

    public BookingNotFoundException(String message) {
        super(message);
        this.bookingId = null;
    }

    public UUID getBookingId() {
        return bookingId;
    }
}
