package com.eduwork.schedule.presentation.controller;

import com.eduwork.schedule.application.query.SearchSessionsQuery;
import com.eduwork.schedule.application.usecase.GetSessionDetailsUseCase;
import com.eduwork.schedule.application.usecase.SearchSessionsUseCase;
import com.eduwork.schedule.domain.model.SessionType;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.presentation.dto.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for session discovery and details.
 * 
 * Endpoints:
 * - GET /sessions - Search available sessions
 * - GET /sessions/{id} - Get session details
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "Session discovery endpoints")
public class ScheduleSessionController {

        private final SearchSessionsUseCase searchSessionsUseCase;
        private final GetSessionDetailsUseCase getSessionDetailsUseCase;

        /**
         * Search for available sessions.
         * 
         * @param subject     Optional subject filter
         * @param sessionType Optional session type filter
         * @param minPrice    Optional minimum price
         * @param maxPrice    Optional maximum price
         * @param startAfter  Optional start time filter
         * @param limit       Optional result limit
         * @return List of matching sessions
         */
        @GetMapping
        @Operation(summary = "Search sessions", description = "Search for available sessions with optional filters")
        public ResponseEntity<List<SessionResponse>> searchSessions(
                        @RequestParam(required = false) String subject,
                        @RequestParam(required = false) SessionType sessionType,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) LocalDateTime startAfter,
                        @RequestParam(required = false, defaultValue = "50") Integer limit) {
                log.info("Searching sessions with filters - type: {}, price: {}-{}",
                                sessionType, minPrice, maxPrice);

                SearchSessionsQuery query = new SearchSessionsQuery(
                                subject,
                                sessionType,
                                minPrice,
                                maxPrice,
                                startAfter,
                                null, // startBefore
                                limit);

                List<Session> sessions = searchSessionsUseCase.execute(query);

                List<SessionResponse> response = sessions.stream()
                                .map(SessionResponse::from)
                                .toList();

                return ResponseEntity.ok(response);
        }

        /**
         * Get session details.
         * 
         * @param sessionId Session ID
         * @return Session details
         */
        @GetMapping("/{sessionId}")
        @Operation(summary = "Get session details", description = "Get detailed information about a specific session")
        public ResponseEntity<SessionResponse> getSessionDetails(
                        @PathVariable UUID sessionId) {
                log.info("Getting details for session {}", sessionId);

                Session session = getSessionDetailsUseCase.execute(sessionId);

                return ResponseEntity.ok(SessionResponse.from(session));
        }
}
