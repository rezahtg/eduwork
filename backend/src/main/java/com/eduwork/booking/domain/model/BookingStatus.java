package com.eduwork.booking.domain.model;

/**
 * BookingStatus represents the lifecycle states of a Booking.
 * 
 * State Transitions:
 * PENDING_PAYMENT → PAID → CONFIRMED → IN_SESSION → COMPLETED
 * ↓ ↓ ↓
 * CANCELLED → CANCELLED → CANCELLED → CANCELLED
 * ↓ ↓ ↓
 * (timeout) REFUNDED → REFUNDED
 * 
 * Business Rules:
 * - PENDING_PAYMENT: Booked but not paid (24-hour deadline)
 * - PAID: Payment confirmed, waiting for session confirmation
 * - CONFIRMED: Session will proceed
 * - IN_SESSION: Currently in session
 * - COMPLETED: Session finished successfully
 * - CANCELLED: Cancelled by student, mentor, or system
 * - REFUNDED: Refund processed
 */
public enum BookingStatus {
    /**
     * Booking created, awaiting payment within 24 hours.
     * If not paid, auto-cancelled.
     */
    PENDING_PAYMENT("Pending Payment", "Awaiting payment (24h deadline)"),

    /**
     * Payment received and confirmed.
     * Waiting for session to be confirmed (minimum students met for GROUP).
     */
    PAID("Paid", "Payment confirmed"),

    /**
     * Session is confirmed and will proceed.
     * - For ONE_ON_ONE: Immediately after payment
     * - For GROUP: After minimum students reached
     */
    CONFIRMED("Confirmed", "Session will proceed"),

    /**
     * Student is currently in the session.
     */
    IN_SESSION("In Session", "Currently in session"),

    /**
     * Session completed successfully.
     * Ready for review.
     */
    COMPLETED("Completed", "Session finished"),

    /**
     * Booking cancelled.
     * Reasons: student request, mentor cancellation, payment timeout, minimum not
     * met.
     */
    CANCELLED("Cancelled", "Booking cancelled"),

    /**
     * Refund has been processed and issued.
     * Terminal state.
     */
    REFUNDED("Refunded", "Refund issued");

    private final String displayName;
    private final String description;

    BookingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if booking can be cancelled by student.
     */
    public boolean canBeCancelled() {
        return this == PENDING_PAYMENT || this == PAID || this == CONFIRMED;
    }

    /**
     * Check if booking has been paid.
     */
    public boolean isPaid() {
        return this == PAID || this == CONFIRMED || this == IN_SESSION || this == COMPLETED;
    }

    /**
     * Check if booking is in terminal state (no further transitions).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == REFUNDED;
    }

    /**
     * Check if booking is active (not cancelled or completed).
     */
    public boolean isActive() {
        return !isTerminal() && this != CANCELLED;
    }
}
