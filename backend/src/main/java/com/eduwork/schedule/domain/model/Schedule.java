package com.eduwork.schedule.domain.model;

import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Schedule Aggregate Root
 * Represents a mentor's availability schedule with timezone support for
 * calendar integration.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    private UUID id;
    private UUID mentorId;
    private String title;
    private String description;
    private ScheduleType type;
    private ScheduleStatus status;
    private ZoneId timezone; // For calendar integration (e.g., "Asia/Jakarta")
    private List<TimeSlot> timeSlots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @Builder
    public Schedule(
            UUID id,
            UUID mentorId,
            String title,
            String description,
            ScheduleType type,
            ZoneId timezone,
            List<TimeSlot> timeSlots) {
        this.id = id != null ? id : UUID.randomUUID();
        this.mentorId = requireNonNull(mentorId, "Mentor ID is required");
        this.title = requireNonNull(title, "Title is required");
        this.description = description;
        this.type = type != null ? type : ScheduleType.ONE_TIME;
        this.status = ScheduleStatus.DRAFT;
        this.timezone = timezone != null ? timezone : ZoneId.systemDefault();
        this.timeSlots = new ArrayList<>(timeSlots != null ? timeSlots : Collections.emptyList());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Business Methods ====================

    /**
     * Add a time slot to this schedule.
     * 
     * @throws ScheduleValidationException if the slot overlaps with existing slots
     */
    public void addTimeSlot(TimeSlot slot) {
        requireNonNull(slot, "Time slot is required");

        if (this.status == ScheduleStatus.CANCELLED) {
            throw new ScheduleValidationException("Cannot add time slots to a cancelled schedule");
        }

        // Check for overlaps
        if (hasOverlappingSlot(slot)) {
            throw new ScheduleValidationException("Time slot overlaps with existing slot");
        }

        this.timeSlots.add(slot);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Remove a time slot by its ID.
     */
    public void removeTimeSlot(UUID slotId) {
        requireNonNull(slotId, "Slot ID is required");

        if (this.status == ScheduleStatus.CANCELLED) {
            throw new ScheduleValidationException("Cannot remove time slots from a cancelled schedule");
        }

        this.timeSlots.removeIf(slot -> slot.getId().equals(slotId));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update an existing time slot.
     */
    public void updateTimeSlot(UUID slotId, TimeSlot updatedSlot) {
        requireNonNull(slotId, "Slot ID is required");
        requireNonNull(updatedSlot, "Updated slot is required");

        if (this.status == ScheduleStatus.CANCELLED) {
            throw new ScheduleValidationException("Cannot update time slots in a cancelled schedule");
        }

        // Remove old slot temporarily for overlap check
        TimeSlot oldSlot = timeSlots.stream()
                .filter(slot -> slot.getId().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new ScheduleValidationException("Time slot not found"));

        timeSlots.remove(oldSlot);

        // Check for overlaps with the updated slot
        if (hasOverlappingSlot(updatedSlot)) {
            timeSlots.add(oldSlot); // Restore old slot
            throw new ScheduleValidationException("Updated time slot overlaps with existing slot");
        }

        timeSlots.add(updatedSlot);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Publish this schedule, making it visible to students.
     * 
     * @throws ScheduleValidationException if quality constraints are violated
     */
    public void publish() {
        if (this.status == ScheduleStatus.PUBLISHED) {
            return; // Already published
        }

        if (this.status == ScheduleStatus.CANCELLED) {
            throw new ScheduleValidationException("Cannot publish a cancelled schedule");
        }

        if (this.timeSlots.isEmpty()) {
            throw new ScheduleValidationException("Cannot publish a schedule without time slots");
        }

        this.status = ScheduleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancel this schedule and all its time slots.
     */
    public void cancel() {
        if (this.status == ScheduleStatus.CANCELLED) {
            return; // Already cancelled
        }

        // Cancel all available time slots
        this.timeSlots.forEach(slot -> {
            if (slot.getStatus() == TimeSlotStatus.AVAILABLE) {
                slot.cancel();
            }
        });

        this.status = ScheduleStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unpublish schedule (revert to DRAFT status).
     */
    public void unpublish() {
        if (this.status != ScheduleStatus.PUBLISHED) {
            throw new ScheduleValidationException("Can only unpublish a published schedule");
        }

        this.status = ScheduleStatus.DRAFT;
        this.publishedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Convert a time slot to the schedule's timezone.
     */
    public ZonedDateTime getSlotStartInTimezone(TimeSlot slot) {
        requireNonNull(slot, "Time slot is required");
        return slot.getStartTime().atZone(this.timezone);
    }

    /**
     * Convert a time slot's end time to the schedule's timezone.
     */
    public ZonedDateTime getSlotEndInTimezone(TimeSlot slot) {
        requireNonNull(slot, "Time slot is required");
        return slot.getEndTime().atZone(this.timezone);
    }

    /**
     * Get total hours of available time slots for a specific date.
     */
    public long getTotalHoursForDate(LocalDateTime date) {
        return timeSlots.stream()
                .filter(slot -> slot.isOnDate(date.toLocalDate()))
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .mapToLong(TimeSlot::getDurationMinutes)
                .sum() / 60;
    }

    /**
     * Check if there are any quality constraint violations.
     * Note: Detailed validation is done by QualityConstraintValidator service.
     */
    public boolean hasQualityViolations() {
        // Basic checks - detailed validation in QualityConstraintValidator
        return timeSlots.stream().anyMatch(this::hasOverlappingSlot);
    }

    /**
     * Get all available time slots.
     */
    public List<TimeSlot> getAvailableTimeSlots() {
        return timeSlots.stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .toList();
    }

    /**
     * Get all booked time slots.
     */
    public List<TimeSlot> getBookedTimeSlots() {
        return timeSlots.stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.BOOKED)
                .toList();
    }

    // ==================== Helper Methods ====================

    private boolean hasOverlappingSlot(TimeSlot newSlot) {
        return timeSlots.stream()
                .filter(existing -> !existing.getId().equals(newSlot.getId()))
                .anyMatch(existing -> existing.overlapsWith(newSlot));
    }

    private <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new ScheduleValidationException(message);
        }
        return obj;
    }

    // ==================== Defensive Getters ====================

    public List<TimeSlot> getTimeSlots() {
        return Collections.unmodifiableList(timeSlots);
    }

    // ==================== For Testing/Persistence ====================

    protected void setId(UUID id) {
        this.id = id;
    }

    protected void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    protected void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    protected void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    protected void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
