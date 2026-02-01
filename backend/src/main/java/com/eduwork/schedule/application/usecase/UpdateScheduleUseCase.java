package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.command.UpdateScheduleCommand;
import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.ScheduleStatus;
import com.eduwork.schedule.domain.model.TimeSlot;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import com.eduwork.schedule.domain.service.QualityConstraintValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.UUID;

/**
 * Use case for updating an existing schedule.
 * Only DRAFT schedules can be fully updated.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final QualityConstraintValidator qualityConstraintValidator;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleResponseDTO execute(UpdateScheduleCommand command) {
        log.info("Updating schedule: {}", command.getScheduleId());

        // Fetch schedule
        Schedule schedule = scheduleRepository.findById(command.getScheduleId())
                .orElseThrow(() -> new ScheduleValidationException("Schedule not found"));

        // Authorization check
        if (!schedule.getMentorId().equals(command.getMentorId())) {
            throw new ScheduleValidationException("Not authorized to update this schedule");
        }

        // Only allow full updates for DRAFT schedules
        if (schedule.getStatus() != ScheduleStatus.DRAFT) {
            throw new ScheduleValidationException("Can only update DRAFT schedules");
        }

        // Update basic fields
        if (command.getTitle() != null) {
            // Use reflection or create a setter for title
            updateScheduleField(schedule, "title", command.getTitle());
        }

        if (command.getDescription() != null) {
            updateScheduleField(schedule, "description", command.getDescription());
        }

        if (command.getTimezone() != null) {
            try {
                ZoneId zoneId = ZoneId.of(command.getTimezone());
                updateScheduleField(schedule, "timezone", zoneId);
            } catch (Exception e) {
                throw new ScheduleValidationException("Invalid timezone: " + command.getTimezone());
            }
        }

        // Update time slots
        if (command.getTimeSlotUpdates() != null) {
            updateTimeSlots(schedule, command);
        }

        // Validate quality constraints
        qualityConstraintValidator.validate(schedule);

        // Save
        Schedule updated = scheduleRepository.save(schedule);

        log.info("Schedule updated successfully: {}", updated.getId());
        return scheduleMapper.toResponseDTO(updated);
    }

    private void updateTimeSlots(Schedule schedule, UpdateScheduleCommand command) {
        for (UpdateScheduleCommand.TimeSlotUpdate update : command.getTimeSlotUpdates()) {
            if (update.isDelete() && update.getSlotId() != null) {
                // Delete existing slot
                schedule.removeTimeSlot(update.getSlotId());
            } else if (update.getSlotId() == null) {
                // Add new slot
                TimeSlot newSlot = new TimeSlot(update.getStartTime(), update.getEndTime());
                schedule.addTimeSlot(newSlot);
            } else {
                // Update existing slot
                TimeSlot updatedSlot = new TimeSlot(
                        update.getSlotId(),
                        update.getStartTime(),
                        update.getEndTime(),
                        com.eduwork.schedule.domain.model.TimeSlotStatus.AVAILABLE);
                schedule.updateTimeSlot(update.getSlotId(), updatedSlot);
            }
        }
    }

    // Helper method to update private fields (alternative to adding setters)
    private void updateScheduleField(Schedule schedule, String fieldName, Object value) {
        try {
            var field = Schedule.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(schedule, value);
        } catch (Exception e) {
            log.error("Failed to update field: {}", fieldName, e);
            throw new ScheduleValidationException("Failed to update schedule field: " + fieldName);
        }
    }
}
