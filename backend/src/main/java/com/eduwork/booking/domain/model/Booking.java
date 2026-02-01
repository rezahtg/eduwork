package com.eduwork.booking.domain.model;

import com.eduwork.booking.domain.exception.PaymentDeadlineExpiredException;
import com.eduwork.common.domain.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Booking represents a student's enrollment in a specific Session.
 * 
 * Aggregate Root for the Booking bounded context.
 * 
 * Business Rules:
 * - Platform fee: 15% of amount paid (non-refundable)
 * - Mentor payout: 85% of amount paid
 * - Payment deadline: 24 hours after booking
 * - Refunds deduct platform fee first, then apply tier percentage
 * - Status transitions managed by domain logic
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {
    private UUID id;
    private UUID sessionId;
    private UUID studentId;

    // Financial
    private Money amountPaid;
    private Money platformFee; // 15% of amountPaid
    private Money mentorPayout; // 85% of amountPaid

    // Payment
    private BookingStatus status;
    private LocalDateTime paymentDeadline;
    private String paymentReference; // External payment gateway reference

    // Refund
    private Money refundAmount;
    private LocalDateTime refundedAt;
    private CancellationReason cancellationReason;

    // Timestamps
    private LocalDateTime bookedAt;
    private LocalDateTime paidAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new booking.
     * Status starts as PENDING_PAYMENT.
     * 
     * @param sessionId       Session being booked
     * @param studentId       Student making the booking
     * @param amount          Amount to pay
     * @param paymentDeadline Deadline for payment (typically 24h from now)
     */
    public static Booking create(
            UUID sessionId,
            UUID studentId,
            Money amount,
            LocalDateTime paymentDeadline) {
        Booking booking = new Booking();
        booking.id = UUID.randomUUID();
        booking.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        booking.studentId = Objects.requireNonNull(studentId, "studentId cannot be null");
        booking.amountPaid = Objects.requireNonNull(amount, "amount cannot be null");
        booking.paymentDeadline = Objects.requireNonNull(paymentDeadline, "paymentDeadline cannot be null");

        // Calculate platform fee (15%) and mentor payout (85%)
        booking.platformFee = amount.multiply(0.15);
        booking.mentorPayout = amount.multiply(0.85);

        booking.status = BookingStatus.PENDING_PAYMENT;
        booking.bookedAt = LocalDateTime.now();

        return booking;
    }

    // ==================== Business Methods ====================

    /**
     * Confirm payment received.
     * Transitions from PENDING_PAYMENT to PAID.
     * 
     * @throws IllegalStateException           if not PENDING_PAYMENT
     * @throws PaymentDeadlineExpiredException if deadline passed
     */
    public void confirmPayment() {
        if (status != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Can only confirm payment for PENDING_PAYMENT bookings, current status: " + status);
        }
        if (LocalDateTime.now().isAfter(paymentDeadline)) {
            throw new PaymentDeadlineExpiredException(paymentDeadline);
        }

        this.status = BookingStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Confirm booking (session minimum reached for GROUP, or immediately for
     * ONE_ON_ONE).
     * Transitions from PAID to CONFIRMED.
     * 
     * @throws IllegalStateException if not PAID
     */
    public void confirm() {
        if (status != BookingStatus.PAID) {
            throw new IllegalStateException(
                    "Can only confirm PAID bookings, current status: " + status);
        }
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * Cancel booking with refund.
     * 
     * @param reason       Reason for cancellation
     * @param refundAmount Calculated refund amount
     * @throws IllegalStateException if already cancelled or in terminal state
     */
    public void cancel(CancellationReason reason, Money refundAmount) {
        if (isCancelled()) {
            throw new IllegalStateException("Booking already cancelled");
        }
        if (status == BookingStatus.IN_SESSION) {
            // Emergency cancellation during session - requires admin review
            if (reason != CancellationReason.EMERGENCY && reason != CancellationReason.ADMIN_ACTION) {
                throw new IllegalStateException(
                        "Cannot cancel during session without emergency or admin action");
            }
        }

        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.refundAmount = refundAmount;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Process refund (mark as refunded).
     * Transitions from CANCELLED to REFUNDED.
     * 
     * @throws IllegalStateException if not cancelled or no refund amount
     */
    public void processRefund() {
        if (status != BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Can only refund CANCELLED bookings, current status: " + status);
        }
        if (refundAmount == null || refundAmount.isZero()) {
            throw new IllegalStateException("No refund amount set");
        }

        this.status = BookingStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * Mark booking as in-session (session has started).
     * Transitions from CONFIRMED to IN_SESSION.
     */
    public void markInSession() {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Can only mark CONFIRMED bookings as in-session, current status: " + status);
        }
        this.status = BookingStatus.IN_SESSION;
    }

    /**
     * Mark booking as completed (session has finished).
     * Transitions from IN_SESSION to COMPLETED.
     */
    public void markCompleted() {
        if (status != BookingStatus.IN_SESSION) {
            throw new IllegalStateException(
                    "Can only complete IN_SESSION bookings, current status: " + status);
        }
        this.status = BookingStatus.COMPLETED;
    }

    /**
     * Set payment reference from external payment gateway.
     */
    public void setPaymentReference(String reference) {
        this.paymentReference = reference;
    }

    // ==================== Query Methods ====================

    /**
     * Check if booking is cancelled.
     */
    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED || status == BookingStatus.REFUNDED;
    }

    /**
     * Check if payment has been made.
     */
    public boolean isPaid() {
        return status == BookingStatus.PAID
                || status == BookingStatus.CONFIRMED
                || status == BookingStatus.IN_SESSION
                || status == BookingStatus.COMPLETED;
    }

    /**
     * Check if payment deadline has expired.
     */
    public boolean isPaymentExpired() {
        return status == BookingStatus.PENDING_PAYMENT
                && LocalDateTime.now().isAfter(paymentDeadline);
    }

    /**
     * Check if booking is in terminal state (no further transitions).
     */
    public boolean isTerminal() {
        return status == BookingStatus.COMPLETED || status == BookingStatus.REFUNDED;
    }

    /**
     * Check if booking is active (not cancelled or completed).
     */
    public boolean isActive() {
        return !isTerminal() && status != BookingStatus.CANCELLED;
    }

    /**
     * Check if eligible for review (session completed).
     */
    public boolean canReview() {
        return status == BookingStatus.COMPLETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Booking booking))
            return false;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Booking[id=%s, session=%s, student=%s, status=%s, amount=%s]",
                id, sessionId, studentId, status, amountPaid);
    }
}
