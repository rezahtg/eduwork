package com.eduwork.schedule.domain.repository;

import com.eduwork.schedule.domain.model.SessionStatus;
import com.eduwork.schedule.domain.model.session.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Session aggregate.
 * Defines persistence operations without exposing infrastructure details.
 */
public interface SessionRepository {

    /**
     * Save a new session or update existing.
     */
    Session save(Session session);

    /**
     * Find session by ID.
     */
    Optional<Session> findById(UUID id);

    /**
     * Find all sessions for a schedule.
     */
    List<Session> findByScheduleId(UUID scheduleId);

    /**
     * Find sessions by status.
     */
    List<Session> findByStatus(SessionStatus status);

    /**
     * Find OPEN sessions available for booking (start time in future).
     */
    List<Session> findOpenSessionsAfter(LocalDateTime startTime);

    /**
     * Find sessions that should auto-start (confirmed, past start time).
     */
    List<Session> findSessionsToStart(LocalDateTime now);

    /**
     * Find sessions that should auto-complete (in progress, past end time).
     */
    List<Session> findSessionsToComplete(LocalDateTime now);

    /**
     * Delete session.
     */
    void delete(Session session);

    /**
     * Check if session exists.
     */
    boolean existsById(UUID id);
}
