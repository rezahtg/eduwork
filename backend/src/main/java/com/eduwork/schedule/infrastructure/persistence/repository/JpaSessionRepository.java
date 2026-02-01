package com.eduwork.schedule.infrastructure.persistence.repository;

import com.eduwork.schedule.domain.model.SessionStatus;
import com.eduwork.schedule.infrastructure.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SessionEntity.
 * Provides query methods for session management and discovery.
 */
@Repository
public interface JpaSessionRepository extends JpaRepository<SessionEntity, UUID> {

    /**
     * Find all sessions for a specific schedule.
     */
    List<SessionEntity> findByScheduleIdOrderByStartTimeAsc(UUID scheduleId);

    /**
     * Find sessions by status.
     */
    List<SessionEntity> findByStatus(SessionStatus status);

    /**
     * Find OPEN sessions starting after a specific time.
     * Used for public session discovery.
     */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE s.status = 'OPEN'
            AND s.startTime >= :startTime
            ORDER BY s.startTime ASC
            """)
    List<SessionEntity> findOpenSessionsAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * Find sessions that should auto-start (confirmed sessions past start time).
     */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE s.status = 'CONFIRMED'
            AND s.startTime <= :now
            """)
    List<SessionEntity> findSessionsToStart(@Param("now") LocalDateTime now);

    /**
     * Find sessions that should auto-complete (in-progress sessions past end time).
     */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE s.status = 'IN_PROGRESS'
            AND s.endTime <= :now
            """)
    List<SessionEntity> findSessionsToComplete(@Param("now") LocalDateTime now);

    /**
     * Find session with lock for concurrent booking (pessimistic lock).
     * Alternative to optimistic locking for critical operations.
     */
    @Query("""
            SELECT s FROM SessionEntity s
            WHERE s.id = :sessionId
            """)
    Optional<SessionEntity> findByIdWithLock(@Param("sessionId") UUID sessionId);
}
