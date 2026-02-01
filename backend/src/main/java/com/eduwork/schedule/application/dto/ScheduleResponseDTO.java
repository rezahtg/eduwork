package com.eduwork.schedule.application.dto;

import com.eduwork.schedule.domain.model.ScheduleStatus;
import com.eduwork.schedule.domain.model.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO {

    private UUID id;
    private UUID mentorId;
    private String title;
    private String description;
    private ScheduleType type;
    private ScheduleStatus status;
    private String timezone;
    private List<TimeSlotDTO> timeSlots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDTO {
        private UUID id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private UUID bookingId;
        private long durationMinutes;
    }
}
