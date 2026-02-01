package com.eduwork.booking.domain.exception;

import java.time.LocalDateTime;

/**
 * Thrown when attempting to confirm payment after the deadline has passed.
 */
public class PaymentDeadlineExpiredException extends RuntimeException {
    private final LocalDateTime deadline;

    public PaymentDeadlineExpiredException(LocalDateTime deadline) {
        super(String.format("Payment deadline %s has expired", deadline));
        this.deadline = deadline;
    }

    public PaymentDeadlineExpiredException(String message) {
        super(message);
        this.deadline = null;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }
}
