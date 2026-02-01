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
 * Use case for getting schedule details by ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetScheduleDetailsUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public ScheduleResponseDTO execute(UUID scheduleId) {
        log.debug("Fetching schedule details: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleValidationException("Schedule not found"));

        return scheduleMapper.toResponseDTO(schedule);
    }
}
