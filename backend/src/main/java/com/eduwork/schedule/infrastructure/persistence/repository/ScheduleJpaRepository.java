package com.eduwork.schedule.infrastructure.persistence.repository;

import com.eduwork.schedule.infrastructure.persistence.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ScheduleEntity.
 */
@Repository
public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, UUID> {

    /**
     * Find all schedules for a specific mentor.
     */
    List<ScheduleEntity> findByMentorId(UUID mentorId);

    /**
     * Find schedules by mentor ID and status.
     */
    List<ScheduleEntity> findByMentorIdAndStatus(
            UUID mentorId,
            ScheduleEntity.ScheduleStatusEnum status);

    /**
     * Find published schedules within a date range.
     * Uses time slots for date filtering.
     */
    @Query("""
                SELECT DISTINCT s FROM ScheduleEntity s
                LEFT JOIN FETCH s.timeSlots ts
                WHERE s.status = 'PUBLISHED'
                AND ts.startTime >= :startDate
                AND ts.endTime <= :endDate
                AND ts.status = 'AVAILABLE'
                ORDER BY ts.startTime
            """)
    List<ScheduleEntity> findPublishedSchedulesInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all published schedules (for batch indexing).
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.status = 'PUBLISHED'")
    List<ScheduleEntity> findAllPublished();

    /**
     * Count schedules by mentor ID and status.
     */
    long countByMentorIdAndStatus(UUID mentorId, ScheduleEntity.ScheduleStatusEnum status);
}
