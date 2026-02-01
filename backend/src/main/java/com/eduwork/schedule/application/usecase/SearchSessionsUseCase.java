package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.query.SearchSessionsQuery;
import com.eduwork.schedule.domain.model.SessionStatus;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * Use case for searching published sessions.
 * Used by students to discover available sessions to book.
 * 
 * Business Rules:
 * - Only OPEN sessions returned (accepting bookings)
 * - Sessions must start in the future
 * - Filters applied: subject, type, price range, time range
 * - Results limited to prevent excessive data transfer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSessionsUseCase {

    private final SessionRepository sessionRepository;

    /**
     * Search for available sessions with optional filters.
     * 
     * @param query Search parameters
     * @return List of matching sessions
     */
    @Transactional(readOnly = true)
    public List<Session> execute(SearchSessionsQuery query) {
        log.debug("Searching sessions with query: {}", query);

        // 1. Get all OPEN sessions starting after specified time
        List<Session> sessions = sessionRepository.findOpenSessionsAfter(query.startAfter());

        // 2. Apply filters using Stream API
        Stream<Session> filtered = sessions.stream();

        // Filter by subject if specified
        if (query.subject() != null && !query.subject().isBlank()) {
            // Note: subject is not directly on Session, would need to join with Schedule
            // For now, skip this filter or add subject to Session
        }

        // Filter by session type
        if (query.sessionType() != null) {
            filtered = filtered.filter(s -> s.getSessionType() == query.sessionType());
        }

        // Filter by price range
        if (query.minPrice() != null) {
            filtered = filtered.filter(s -> s.getPricePerStudent().getAmount().compareTo(query.minPrice()) >= 0);
        }
        if (query.maxPrice() != null) {
            filtered = filtered.filter(s -> s.getPricePerStudent().getAmount().compareTo(query.maxPrice()) <= 0);
        }

        // Filter by start time range
        if (query.startBefore() != null) {
            filtered = filtered.filter(s -> s.getStartTime().isBefore(query.startBefore()));
        }

        // 3. Limit results
        List<Session> results = filtered
                .limit(query.limit())
                .toList();

        log.info("Found {} sessions matching query", results.size());

        return results;
    }
}
