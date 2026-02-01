package com.eduwork.booking.application.command;

import java.util.UUID;

/**
 * Command to confirm payment for a booking.
 */
public record ConfirmPaymentCommand(
        UUID bookingId,
        String paymentReference // From payment gateway
) {
    public ConfirmPaymentCommand {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new IllegalArgumentException("Payment reference cannot be null or empty");
        }
    }
}
