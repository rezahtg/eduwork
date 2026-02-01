package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.application.query.SearchSchedulesQuery;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case for searching published schedules.
 * Will be enhanced with Elasticsearch for advanced search capabilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchSchedulesUseCase {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> execute(SearchSchedulesQuery query) {
        log.debug("Searching schedules with query: {}", query);

        // For now, use simple repository query
        // TODO: Replace with Elasticsearch when implemented
        LocalDateTime start = query.getStartDate() != null ? query.getStartDate() : LocalDateTime.now();
        LocalDateTime end = query.getEndDate() != null ? query.getEndDate() : start.plusMonths(1);

        List<Schedule> schedules = scheduleRepository.findPublishedSchedules(start, end);

        // Apply keyword filtering (simple implementation)
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().toLowerCase();
            schedules = schedules.stream()
                    .filter(s -> s.getTitle().toLowerCase().contains(keyword) ||
                            (s.getDescription() != null && s.getDescription().toLowerCase().contains(keyword)))
                    .toList();
        }

        // Apply timezone filtering
        if (query.getTimezone() != null) {
            schedules = schedules.stream()
                    .filter(s -> s.getTimezone().getId().equals(query.getTimezone()))
                    .toList();
        }

        // Simple pagination (in-memory)
        // TODO: Move to database/Elasticsearch when implemented
        int page = query.getValidatedPage();
        int size = query.getValidatedSize();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, schedules.size());

        if (fromIndex >= schedules.size()) {
            return List.of();
        }

        return schedules.subList(fromIndex, toIndex).stream()
                .map(scheduleMapper::toResponseDTO)
                .toList();
    }
}
