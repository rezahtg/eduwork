package com.eduwork.schedule.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Query for searching schedules.
 * Used by students to discover available mentoring sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSchedulesQuery {

    private String keyword; // Search in title, description
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timezone; // Filter by timezone
    private Integer page;
    private Integer size;

    public int getValidatedPage() {
        return page != null && page > 0 ? page : 0;
    }

    public int getValidatedSize() {
        if (size == null || size <= 0) {
            return 20; // Default page size
        }
        return Math.min(size, 100); // Max page size
    }
}
