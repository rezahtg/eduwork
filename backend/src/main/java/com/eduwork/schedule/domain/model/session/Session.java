package com.eduwork.schedule.domain.model.session;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.model.BookingStatus;
import com.eduwork.booking.domain.model.CancellationReason;
import com.eduwork.booking.domain.exception.BookingNotFoundException;
import com.eduwork.booking.domain.exception.DuplicateBookingException;
import com.eduwork.booking.domain.model.RefundPolicy;
import com.eduwork.common.domain.Money;
import com.eduwork.schedule.domain.exception.SessionFullException;
import com.eduwork.schedule.domain.model.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Session is a specific bookable instance created from a Schedule template.
 * Students book Sessions, not Schedules.
 * 
 * Aggregate Root with optimistic locking to prevent race conditions.
 * 
 * Business Rules:
 * - ONE_ON_ONE: Exactly 1 booking allowed
 * - GROUP: Multiple bookings up to maxStudents
 * - Optimistic locking (@Version) prevents overbooking on last slot
 * - Payment deadline: 24 hours after booking
 * - Status transitions managed by domain logic
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {
    private UUID id;
    private UUID scheduleId;

    // Time & Location
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Configuration (denormalized from Schedule at creation)
    private SessionType sessionType;
    private Capacity capacity;
    private Money pricePerStudent;

    // State Management
    private SessionStatus status;
    private Long version; // Optimistic locking - will be managed by JPA

    // Current enrollment
    private int currentEnrollment = 0;

    // Bookings (aggregate children)
    @Getter(AccessLevel.NONE)
    private List<Booking> bookings = new ArrayList<>();

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new Session from a Schedule template.
     */
    public static Session create(
            UUID scheduleId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            SessionType sessionType,
            Capacity capacity,
            Money pricePerStudent) {
        Session session = new Session();
        session.id = UUID.randomUUID();
        session.scheduleId = Objects.requireNonNull(scheduleId, "scheduleId cannot be null");
        session.startTime = Objects.requireNonNull(startTime, "startTime cannot be null");
        session.endTime = Objects.requireNonNull(endTime, "endTime cannot be null");
        session.sessionType = Objects.requireNonNull(sessionType, "sessionType cannot be null");
        session.capacity = Objects.requireNonNull(capacity, "capacity cannot be null");
        session.pricePerStudent = Objects.requireNonNull(pricePerStudent, "pricePerStudent cannot be null");
        session.status = SessionStatus.DRAFT;
        session.currentEnrollment = 0;
        session.createdAt = LocalDateTime.now();
        session.updatedAt = LocalDateTime.now();

        // Validate time range
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        return session;
    }

    // ==================== Business Methods ====================

    /**
     * Publish this session, making it available for booking.
     */
    public void publish() {
        if (status != SessionStatus.DRAFT) {
            throw new IllegalStateException("Can only publish DRAFT sessions, current status: " + status);
        }
        this.status = SessionStatus.OPEN;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Book this session for a student.
     * Uses optimistic locking to prevent race conditions on last slot.
     * 
     * @param studentId Student booking the session
     * @param amount    Amount to pay
     * @return Created booking
     * @throws SessionFullException      if session is full
     * @throws DuplicateBookingException if student already booked
     */
    public Booking book(UUID studentId, Money amount) {
        // Check availability
        if (!canAcceptBooking()) {
            throw new SessionFullException(
                    String.format("Session %s is full or not accepting bookings (status: %s, enrollment: %d/%d)",
                            id, status, currentEnrollment, capacity.maxStudents()));
        }

        // Check duplicate booking
        boolean alreadyBooked = bookings.stream()
                .anyMatch(b -> b.getStudentId().equals(studentId) && !b.isCancelled());
        if (alreadyBooked) {
            throw new DuplicateBookingException(id, studentId);
        }

        // Create booking
        Booking booking = Booking.create(
                this.id,
                studentId,
                amount,
                calculatePaymentDeadline());

        bookings.add(booking);
        currentEnrollment++;
        updateStatusAfterBooking();
        this.updatedAt = LocalDateTime.now();

        return booking;
    }

    /**
     * Confirm payment for a booking.
     * Triggers session confirmation if minimum reached.
     */
    public void confirmBooking(UUID bookingId) {
        Booking booking = findBooking(bookingId);
        booking.confirmPayment();

        // Check if minimum reached
        if (isMinimumReached() && status == SessionStatus.WAITING) {
            this.status = SessionStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
            // TODO: Domain event - MinimumReachedEvent to notify students
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancel a booking with refund calculation.
     */
    public void cancelBooking(UUID bookingId, CancellationReason reason) {
        Booking booking = findBooking(bookingId);

        // Calculate refund based on timing
        RefundPolicy refundPolicy = RefundPolicy.standard();
        Money refundAmount = refundPolicy.calculateRefund(
                booking.getAmountPaid(),
                Duration.between(LocalDateTime.now(), startTime));

        booking.cancel(reason, refundAmount);
        currentEnrollment--;
        updateStatusAfterCancellation();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Start the session (manual or auto-triggered).
     */
    public void start() {
        if (status != SessionStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Can only start CONFIRMED sessions, current status: " + status);
        }
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Mark all confirmed bookings as IN_SESSION
        bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .forEach(Booking::markInSession);
    }

    /**
     * Complete the session (manual or auto-triggered).
     */
    public void complete() {
        if (status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Can only complete IN_PROGRESS sessions, current status: " + status);
        }
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Mark all in-session bookings as COMPLETED
        bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.IN_SESSION)
                .forEach(Booking::markCompleted);
    }

    /**
     * Cancel the entire session (mentor action).
     * Issues full refunds to all students.
     */
    public void cancel() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot cancel terminal session: " + status);
        }

        this.status = SessionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        // Cancel all active bookings with full refund
        bookings.stream()
                .filter(b -> b.isActive())
                .forEach(b -> b.cancel(CancellationReason.MENTOR_CANCELLED, b.getAmountPaid()));
    }

    // ==================== Query Methods ====================

    /**
     * Check if session can accept new bookings.
     */
    public boolean canAcceptBooking() {
        return status == SessionStatus.OPEN
                && currentEnrollment < capacity.maxStudents()
                && startTime.isAfter(LocalDateTime.now().plusHours(24)); // At least 24h notice
    }

    /**
     * Check if minimum students requirement is met.
     */
    public boolean isMinimumReached() {
        long paidBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID
                        || b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.IN_SESSION
                        || b.getStatus() == BookingStatus.COMPLETED)
                .count();
        return paidBookings >= capacity.minStudents();
    }

    /**
     * Check if session is full.
     */
    public boolean isFull() {
        return currentEnrollment >= capacity.maxStudents();
    }

    /**
     * Get number of available slots.
     */
    public int getAvailableSlots() {
        return capacity.maxStudents() - currentEnrollment;
    }

    /**
     * Get all bookings (defensive copy).
     */
    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    /**
     * Get bookings for display purposes (read-only).
     */
    public List<Booking> getActiveBookings() {
        return bookings.stream()
                .filter(Booking::isActive)
                .toList();
    }

    // ==================== Helper Methods ====================

    /**
     * Update session status after a new booking.
     */
    private void updateStatusAfterBooking() {
        if (sessionType == SessionType.ONE_ON_ONE) {
            // ONE_ON_ONE sessions are immediately confirmed after booking
            this.status = SessionStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
        } else if (sessionType == SessionType.GROUP) {
            // GROUP sessions wait for minimum or confirm if already met
            if (isMinimumReached()) {
                this.status = SessionStatus.CONFIRMED;
                this.confirmedAt = LocalDateTime.now();
            } else {
                this.status = SessionStatus.WAITING;
            }
        }
    }

    /**
     * Update session status after a booking cancellation.
     */
    private void updateStatusAfterCancellation() {
        if (sessionType == SessionType.GROUP && !isMinimumReached()) {
            this.status = SessionStatus.WAITING;
            // TODO: Domain event - BelowMinimumEvent to notify mentor and students
        }
    }

    /**
     * Calculate payment deadline (24 hours from now).
     */
    private LocalDateTime calculatePaymentDeadline() {
        return LocalDateTime.now().plusHours(24);
    }

    /**
     * Find booking by ID.
     */
    private Booking findBooking(UUID bookingId) {
        return bookings.stream()
                .filter(b -> b.getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Session session))
            return false;
        return Objects.equals(id, session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Session[id=%s, type=%s, status=%s, enrollment=%d/%d]",
                id, sessionType, status, currentEnrollment, capacity.maxStudents());
    }
}
