package com.eduwork.schedule.domain.service;

import com.eduwork.schedule.domain.exception.QualityConstraintViolationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.TimeSlot;
import com.eduwork.schedule.domain.model.TimeSlotStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain Service for validating schedule quality constraints.
 * 
 * Business Rules Enforced:
 * 1. Maximum 8 hours of mentoring per day
 * 2. Minimum 15-minute rest between consecutive sessions
 * 3. No overlapping time slots
 */
@Slf4j
@Service
public class QualityConstraintValidator {

    private static final long MAX_HOURS_PER_DAY = 8;
    private static final long MIN_REST_MINUTES = 15;
    private static final long MIN_SLOT_MINUTES = 30;
    private static final long MAX_SLOT_MINUTES = 480; // 8 hours

    /**
     * Validate all quality constraints for a schedule.
     * 
     * @throws QualityConstraintViolationException if any constraints are violated
     */
    public void validate(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        violations.addAll(validateMaxDailyHours(schedule));
        violations.addAll(validateRestPeriods(schedule));
        violations.addAll(validateOverlaps(schedule));
        violations.addAll(validateSlotDurations(schedule));

        if (!violations.isEmpty()) {
            log.warn("Quality constraint violations found for schedule {}: {}",
                    schedule.getId(), violations);
            throw new QualityConstraintViolationException(violations);
        }

        log.debug("Schedule {} passed all quality constraints", schedule.getId());
    }

    /**
     * Validate just the max hours constraint (lightweight check).
     * Returns violations if any, empty list otherwise.
     */
    public List<String> validateMaxDailyHours(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        Map<LocalDate, Long> hoursPerDay = calculateHoursPerDay(schedule);

        for (Map.Entry<LocalDate, Long> entry : hoursPerDay.entrySet()) {
            if (entry.getValue() > MAX_HOURS_PER_DAY) {
                violations.add(String.format(
                        "Exceeds maximum %d hours per day on %s (total: %d hours)",
                        MAX_HOURS_PER_DAY, entry.getKey(), entry.getValue()));
            }
        }

        return violations;
    }

    /**
     * Validate minimum rest periods between consecutive sessions.
     */
    public List<String> validateRestPeriods(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        List<TimeSlot> availableSlots = schedule.getTimeSlots().stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .sorted(Comparator.comparing(TimeSlot::getStartTime))
                .toList();

        for (int i = 0; i < availableSlots.size() - 1; i++) {
            TimeSlot current = availableSlots.get(i);
            TimeSlot next = availableSlots.get(i + 1);

            // Check if they're on the same day
            if (current.getStartTime().toLocalDate().equals(next.getStartTime().toLocalDate())) {
                long gapMinutes = Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

                if (gapMinutes < MIN_REST_MINUTES && gapMinutes >= 0) {
                    violations.add(String.format(
                            "Insufficient rest between slots at %s and %s (gap: %d minutes, required: %d minutes)",
                            current.getEndTime(), next.getStartTime(), gapMinutes, MIN_REST_MINUTES));
                }
            }
        }

        return violations;
    }

    /**
     * Validate no overlapping time slots.
     */
    public List<String> validateOverlaps(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        List<TimeSlot> slots = schedule.getTimeSlots();

        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                TimeSlot slot1 = slots.get(i);
                TimeSlot slot2 = slots.get(j);

                if (slot1.overlapsWith(slot2)) {
                    violations.add(String.format(
                            "Time slots overlap: [%s - %s] and [%s - %s]",
                            slot1.getStartTime(), slot1.getEndTime(),
                            slot2.getStartTime(), slot2.getEndTime()));
                }
            }
        }

        return violations;
    }

    /**
     * Validate individual slot duration constraints.
     */
    public List<String> validateSlotDurations(Schedule schedule) {
        List<String> violations = new ArrayList<>();

        for (TimeSlot slot : schedule.getTimeSlots()) {
            long minutes = slot.getDurationMinutes();

            if (minutes < MIN_SLOT_MINUTES) {
                violations.add(String.format(
                        "Time slot too short: [%s - %s] (%d minutes, minimum: %d minutes)",
                        slot.getStartTime(), slot.getEndTime(), minutes, MIN_SLOT_MINUTES));
            }

            if (minutes > MAX_SLOT_MINUTES) {
                violations.add(String.format(
                        "Time slot too long: [%s - %s] (%d minutes, maximum: %d minutes)",
                        slot.getStartTime(), slot.getEndTime(), minutes, MAX_SLOT_MINUTES));
            }
        }

        return violations;
    }

    /**
     * Check if adding a new time slot would violate constraints.
     * Returns violations if any, empty list otherwise.
     */
    public List<String> validateNewSlot(Schedule schedule, TimeSlot newSlot) {
        List<String> violations = new ArrayList<>();

        // Create temporary schedule with new slot for validation
        List<TimeSlot> tempSlots = new ArrayList<>(schedule.getTimeSlots());
        tempSlots.add(newSlot);

        // Validate daily hours with new slot
        LocalDate slotDate = newSlot.getStartTime().toLocalDate();
        long totalHours = tempSlots.stream()
                .filter(slot -> slot.isOnDate(slotDate))
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .mapToLong(TimeSlot::getDurationMinutes)
                .sum() / 60;

        if (totalHours > MAX_HOURS_PER_DAY) {
            violations.add(String.format(
                    "Adding this slot would exceed maximum %d hours per day on %s (total: %d hours)",
                    MAX_HOURS_PER_DAY, slotDate, totalHours));
        }

        // Validate no overlaps
        for (TimeSlot existing : schedule.getTimeSlots()) {
            if (existing.overlapsWith(newSlot)) {
                violations.add(String.format(
                        "New slot [%s - %s] overlaps with existing slot [%s - %s]",
                        newSlot.getStartTime(), newSlot.getEndTime(),
                        existing.getStartTime(), existing.getEndTime()));
            }
        }

        // Validate rest periods
        violations.addAll(validateRestPeriodsForNewSlot(schedule, newSlot));

        // Validate slot duration
        long minutes = newSlot.getDurationMinutes();
        if (minutes < MIN_SLOT_MINUTES) {
            violations.add(String.format(
                    "Time slot too short: %d minutes (minimum: %d minutes)",
                    minutes, MIN_SLOT_MINUTES));
        }
        if (minutes > MAX_SLOT_MINUTES) {
            violations.add(String.format(
                    "Time slot too long: %d minutes (maximum: %d minutes)",
                    minutes, MAX_SLOT_MINUTES));
        }

        return violations;
    }

    /**
     * Validate rest periods if a new slot is added.
     */
    private List<String> validateRestPeriodsForNewSlot(Schedule schedule, TimeSlot newSlot) {
        List<String> violations = new ArrayList<>();

        LocalDate newSlotDate = newSlot.getStartTime().toLocalDate();

        List<TimeSlot> sameDaySlots = schedule.getTimeSlots().stream()
                .filter(slot -> slot.isOnDate(newSlotDate))
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .toList();

        for (TimeSlot existing : sameDaySlots) {
            long gap = newSlot.getGapMinutes(existing);

            if (gap >= 0 && gap < MIN_REST_MINUTES) {
                violations.add(String.format(
                        "Insufficient rest between new slot and existing slot at %s (gap: %d minutes, required: %d minutes)",
                        existing.getStartTime(), gap, MIN_REST_MINUTES));
            }
        }

        return violations;
    }

    /**
     * Calculate total hours of available slots per day.
     */
    private Map<LocalDate, Long> calculateHoursPerDay(Schedule schedule) {
        return schedule.getTimeSlots().stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.AVAILABLE)
                .collect(Collectors.groupingBy(
                        slot -> slot.getStartTime().toLocalDate(),
                        Collectors.summingLong(slot -> slot.getDurationMinutes() / 60)));
    }

    /**
     * Get quality constraint configuration (for informational purposes).
     */
    public Map<String, Object> getConstraints() {
        return Map.of(
                "maxHoursPerDay", MAX_HOURS_PER_DAY,
                "minRestMinutes", MIN_REST_MINUTES,
                "minSlotMinutes", MIN_SLOT_MINUTES,
                "maxSlotMinutes", MAX_SLOT_MINUTES);
    }
}
