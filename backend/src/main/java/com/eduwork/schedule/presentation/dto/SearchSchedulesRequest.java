package com.eduwork.schedule.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for searching schedules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSchedulesRequest {

    private String keyword; // Search in title, description
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timezone;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
