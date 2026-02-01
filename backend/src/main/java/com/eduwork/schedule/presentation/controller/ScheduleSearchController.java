package com.eduwork.schedule.presentation.controller;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.application.query.SearchSchedulesQuery;
import com.eduwork.schedule.application.usecase.SearchSchedulesUseCase;
import com.eduwork.schedule.presentation.dto.SearchSchedulesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for student schedule discovery.
 * Endpoints:
 * - GET /api/v1/schedules/search - Search published schedules
 */
@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleSearchController {

    private final SearchSchedulesUseCase searchSchedulesUseCase;

    /**
     * Search published schedules (student discovery).
     */
    @GetMapping("/search")
    public ResponseEntity<List<ScheduleResponseDTO>> searchSchedules(
            @ModelAttribute SearchSchedulesRequest request) {
        log.info("Searching schedules with keyword: {}", request.getKeyword());

        SearchSchedulesQuery query = SearchSchedulesQuery.builder()
                .keyword(request.getKeyword())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .timezone(request.getTimezone())
                .page(request.getPage())
                .size(request.getSize())
                .build();

        List<ScheduleResponseDTO> results = searchSchedulesUseCase.execute(query);

        return ResponseEntity.ok(results);
    }
}
