package com.eduwork.schedule.domain.repository;

import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.ScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Schedule aggregate.
 * Defines the contract for schedule persistence operations.
 */
public interface ScheduleRepository {

    /**
     * Save a schedule (create or update).
     */
    Schedule save(Schedule schedule);

    /**
     * Find a schedule by ID.
     */
    Optional<Schedule> findById(UUID id);

    /**
     * Find all schedules for a specific mentor.
     */
    List<Schedule> findByMentorId(UUID mentorId);

    /**
     * Find schedules by mentor ID and status.
     */
    List<Schedule> findByMentorIdAndStatus(UUID mentorId, ScheduleStatus status);

    /**
     * Find published schedules within a date range.
     * Used for student discovery.
     */
    List<Schedule> findPublishedSchedules(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if a schedule exists by ID.
     */
    boolean existsById(UUID id);

    /**
     * Delete a schedule by ID.
     */
    void deleteById(UUID id);

    /**
     * Find all published schedules (for batch indexing).
     */
    List<Schedule> findAllPublished();

    /**
     * Count schedules by mentor ID and status.
     */
    long countByMentorIdAndStatus(UUID mentorId, ScheduleStatus status);
}
