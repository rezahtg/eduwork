package com.eduwork.schedule.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for published schedule with generated sessions.
 */
@Value
@Builder
public class ScheduleWithSessionsResponseDTO {
    UUID scheduleId;
    String title;
    String status;
    int sessionsCreated;
    List<SessionSummaryDTO> sessions;
}
