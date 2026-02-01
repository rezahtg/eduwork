package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.command.CreateScheduleCommand;
import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.exception.QualityConstraintViolationException;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.TimeSlot;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import com.eduwork.schedule.domain.service.QualityConstraintValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for creating a new schedule.
 * 
 * Business Rules:
 * - Schedule starts in DRAFT status
 * - Quality constraints must be validated
 * - Timezone defaults to system timezone if not provided
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final QualityConstraintValidator qualityConstraintValidator;
    private final ScheduleMapper scheduleMapper;

    /**
     * Execute the use case to create a new schedule.
     * 
     * @param command The create schedule command
     * @return The created schedule response DTO
     * @throws ScheduleValidationException         if validation fails
     * @throws QualityConstraintViolationException if quality constraints are
     *                                             violated
     */
    @Transactional
    public ScheduleResponseDTO execute(CreateScheduleCommand command) {
        log.info("Creating new schedule for mentor: {}", command.getMentorId());

        // Validate command
        validateCommand(command);

        // Build time slots
        List<TimeSlot> timeSlots = command.getTimeSlots().stream()
                .map(slotCmd -> new TimeSlot(
                        slotCmd.getStartTime(),
                        slotCmd.getEndTime()))
                .toList();

        // Create schedule entity
        Schedule schedule = Schedule.builder()
                .mentorId(command.getMentorId())
                .title(command.getTitle())
                .description(command.getDescription())
                .type(com.eduwork.schedule.domain.model.ScheduleType.ONE_TIME)
                .timezone(command.getTimezoneAsZoneId())
                .timeSlots(timeSlots)
                .build();

        // Validate quality constraints
        qualityConstraintValidator.validate(schedule);

        // Save schedule
        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Schedule created successfully: {}", savedSchedule.getId());

        return scheduleMapper.toResponseDTO(savedSchedule);
    }

    private void validateCommand(CreateScheduleCommand command) {
        if (command.getMentorId() == null) {
            throw new ScheduleValidationException("Mentor ID is required");
        }

        if (command.getTitle() == null || command.getTitle().isBlank()) {
            throw new ScheduleValidationException("Title is required");
        }

        if (command.getTimeSlots() == null || command.getTimeSlots().isEmpty()) {
            throw new ScheduleValidationException("At least one time slot is required");
        }

        // Validate timezone if provided
        if (command.getTimezone() != null) {
            try {
                command.getTimezoneAsZoneId();
            } catch (Exception e) {
                throw new ScheduleValidationException("Invalid timezone: " + command.getTimezone());
            }
        }
    }
}
