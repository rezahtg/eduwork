package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for getting all schedules for a mentor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMySchedulesUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> execute(UUID mentorId) {
        log.debug("Fetching schedules for mentor: {}", mentorId);

        List<Schedule> schedules = scheduleRepository.findByMentorId(mentorId);

        return schedules.stream()
                .map(scheduleMapper::toResponseDTO)
                .toList();
    }
}
