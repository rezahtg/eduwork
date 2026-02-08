package com.eduwork.schedule.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for session summary in schedule responses.
 */
@Value
@Builder
public class SessionSummaryDTO {
    UUID id;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String status;
    String sessionType;
    int availableSlots;
    int currentEnrollment;
}
