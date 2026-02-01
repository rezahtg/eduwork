package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for cancelling a schedule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleResponseDTO execute(UUID scheduleId, UUID mentorId) {
        log.info("Cancelling schedule: {}", scheduleId);

        // Fetch schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleValidationException("Schedule not found"));

        // Authorization check
        if (!schedule.getMentorId().equals(mentorId)) {
            throw new ScheduleValidationException("Not authorized to cancel this schedule");
        }

        // Cancel
        schedule.cancel();

        // Save
        Schedule cancelled = scheduleRepository.save(schedule);

        log.info("Schedule cancelled successfully: {}", cancelled.getId());

        // TODO: Trigger async Elasticsearch update/removal here

        return scheduleMapper.toResponseDTO(cancelled);
    }
}
