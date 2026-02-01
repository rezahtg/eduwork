package com.eduwork.schedule.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Command to update an existing schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleCommand {

    private UUID scheduleId;
    private UUID mentorId; // For authorization check
    private String title;
    private String description;
    private String timezone;
    private List<TimeSlotUpdate> timeSlotUpdates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotUpdate {
        private UUID slotId; // null for new slots
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean delete; // true to delete this slot
    }
}
