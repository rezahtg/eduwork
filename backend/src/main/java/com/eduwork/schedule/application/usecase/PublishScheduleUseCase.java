package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.ScheduleStatus;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import com.eduwork.schedule.domain.service.QualityConstraintValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for publishing a schedule.
 * Makes the schedule visible and available for student booking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublishScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final QualityConstraintValidator qualityConstraintValidator;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleResponseDTO execute(UUID scheduleId, UUID mentorId) {
        log.info("Publishing schedule: {}", scheduleId);

        // Fetch schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleValidationException("Schedule not found"));

        // Authorization check
        if (!schedule.getMentorId().equals(mentorId)) {
            throw new ScheduleValidationException("Not authorized to publish this schedule");
        }

        // Validate quality constraints before publishing
        qualityConstraintValidator.validate(schedule);

        // Publish
        schedule.publish();

        // Save
        Schedule published = scheduleRepository.save(schedule);

        log.info("Schedule published successfully: {}", published.getId());

        // TODO: Trigger async Elasticsearch indexing here

        return scheduleMapper.toResponseDTO(published);
    }
}
