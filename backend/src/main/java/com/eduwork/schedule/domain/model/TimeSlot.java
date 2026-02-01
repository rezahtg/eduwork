package com.eduwork.schedule.domain.model;

import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * TimeSlot Value Object
 * Represents a specific time slot within a schedule.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot {

    private UUID id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TimeSlotStatus status;
    private UUID bookingId; // Reference to booking if booked
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this(UUID.randomUUID(), startTime, endTime, TimeSlotStatus.AVAILABLE);
    }

    public TimeSlot(UUID id, LocalDateTime startTime, LocalDateTime endTime, TimeSlotStatus status) {
        this.id = id != null ? id : UUID.randomUUID();
        this.startTime = requireNonNull(startTime, "Start time is required");
        this.endTime = requireNonNull(endTime, "End time is required");
        this.status = status != null ? status : TimeSlotStatus.AVAILABLE;

        validate();
    }

    // ==================== Business Methods ====================

    /**
     * Mark this time slot as booked.
     */
    public void markAsBooked(UUID bookingId) {
        requireNonNull(bookingId, "Booking ID is required");

        if (this.status != TimeSlotStatus.AVAILABLE) {
            throw new ScheduleValidationException("Can only book available time slots");
        }

        this.status = TimeSlotStatus.BOOKED;
        this.bookingId = bookingId;
        this.bookedAt = LocalDateTime.now();
    }

    /**
     * Cancel this time slot (from AVAILABLE status).
     */
    public void cancel() {
        if (this.status == TimeSlotStatus.BOOKED) {
            throw new ScheduleValidationException("Cannot cancel a booked time slot. Cancel the booking first.");
        }

        if (this.status == TimeSlotStatus.CANCELLED) {
            return; // Already cancelled
        }

        this.status = TimeSlotStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Release this time slot back to AVAILABLE (from BOOKED status).
     */
    public void release() {
        if (this.status != TimeSlotStatus.BOOKED) {
            throw new ScheduleValidationException("Can only release booked time slots");
        }

        this.status = TimeSlotStatus.AVAILABLE;
        this.bookingId = null;
        this.bookedAt = null;
    }

    /**
     * Check if this slot overlaps with another slot.
     */
    public boolean overlapsWith(TimeSlot other) {
        requireNonNull(other, "Other time slot is required");

        // No overlap if one slot ends before the other starts
        return !(this.endTime.isBefore(other.startTime) ||
                this.endTime.equals(other.startTime) ||
                other.endTime.isBefore(this.startTime) ||
                other.endTime.equals(this.startTime));
    }

    /**
     * Get duration in minutes.
     */
    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Get duration in hours.
     */
    public long getDurationHours() {
        return getDurationMinutes() / 60;
    }

    /**
     * Check if this slot is on a specific date.
     */
    public boolean isOnDate(LocalDate date) {
        return startTime.toLocalDate().equals(date);
    }

    /**
     * Check if this slot is available for booking.
     */
    public boolean isAvailable() {
        return this.status == TimeSlotStatus.AVAILABLE &&
                this.startTime.isAfter(LocalDateTime.now());
    }

    /**
     * Check if this slot is in the past.
     */
    public boolean isPast() {
        return this.endTime.isBefore(LocalDateTime.now());
    }

    /**
     * Check if this slot is booked.
     */
    public boolean isBooked() {
        return this.status == TimeSlotStatus.BOOKED;
    }

    /**
     * Get the gap between this slot and another (in minutes).
     * Returns 0 if they overlap or touch.
     * Returns negative if they overlap.
     */
    public long getGapMinutes(TimeSlot other) {
        requireNonNull(other, "Other time slot is required");

        if (this.endTime.isBefore(other.startTime) || this.endTime.equals(other.startTime)) {
            // This slot ends before other starts
            return Duration.between(this.endTime, other.startTime).toMinutes();
        } else if (other.endTime.isBefore(this.startTime) || other.endTime.equals(this.startTime)) {
            // Other slot ends before this starts
            return Duration.between(other.endTime, this.startTime).toMinutes();
        } else {
            // Overlapping or touching
            return 0;
        }
    }

    // ==================== Validation ====================

    private void validate() {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new ScheduleValidationException("Start time must be before end time");
        }

        long durationMinutes = getDurationMinutes();
        if (durationMinutes < 30) {
            throw new ScheduleValidationException("Time slot must be at least 30 minutes");
        }

        if (durationMinutes > 480) { // 8 hours max
            throw new ScheduleValidationException("Time slot cannot exceed 8 hours");
        }
    }

    private <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new ScheduleValidationException(message);
        }
        return obj;
    }

    // ==================== Equality & HashCode ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TimeSlot timeSlot))
            return false;
        return Objects.equals(id, timeSlot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ==================== For Testing/Persistence ====================

    protected void setId(UUID id) {
        this.id = id;
    }

    protected void setStatus(TimeSlotStatus status) {
        this.status = status;
    }

    protected void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    protected void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    protected void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
