package com.eduwork.schedule.presentation.dto;

import com.eduwork.schedule.domain.model.SessionStatus;
import com.eduwork.schedule.domain.model.SessionType;
import com.eduwork.schedule.domain.model.session.Session;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for session details.
 */
public record SessionResponse(
        UUID id,
        UUID scheduleId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SessionType sessionType,
        BigDecimal pricePerStudent,
        String currency,
        Integer minStudents,
        Integer maxStudents,
        SessionStatus status,
        Integer currentEnrollment,
        Integer availableSlots,
        LocalDateTime createdAt) {
    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getScheduleId(),
                session.getStartTime(),
                session.getEndTime(),
                session.getSessionType(),
                session.getPricePerStudent().getAmount(),
                session.getPricePerStudent().getCurrency(),
                session.getCapacity().minStudents(),
                session.getCapacity().maxStudents(),
                session.getStatus(),
                session.getCurrentEnrollment(),
                session.getAvailableSlots(),
                session.getCreatedAt());
    }
}
