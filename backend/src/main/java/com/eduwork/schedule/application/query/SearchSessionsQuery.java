package com.eduwork.schedule.application.query;

import com.eduwork.schedule.domain.model.SessionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Query parameters for searching published sessions.
 * Used for public session discovery by students.
 */
public record SearchSessionsQuery(
        String subject, // Optional: filter by subject
        SessionType sessionType, // Optional: ONE_ON_ONE or GROUP
        BigDecimal minPrice, // Optional: minimum price filter
        BigDecimal maxPrice, // Optional: maximum price filter
        LocalDateTime startAfter, // Optional: sessions starting after this time
        LocalDateTime startBefore, // Optional: sessions starting before this time
        Integer limit // Optional: max results (default 50)
) {
    /**
     * Compact constructor with defaults.
     */
    public SearchSessionsQuery {
        if (limit == null || limit <= 0) {
            limit = 50; // Default limit
        }
        if (limit > 100) {
            limit = 100; // Max limit
        }
        if (startAfter == null) {
            startAfter = LocalDateTime.now(); // Default to future sessions
        }
    }

    /**
     * Create a simple query with just time filter.
     */
    public static SearchSessionsQuery upcoming() {
        return new SearchSessionsQuery(null, null, null, null, LocalDateTime.now(), null, 50);
    }
}
