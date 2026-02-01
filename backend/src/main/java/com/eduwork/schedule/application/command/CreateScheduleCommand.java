package com.eduwork.schedule.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Command to create a new schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleCommand {

    private UUID mentorId;
    private String title;
    private String description;
    private String timezone; // Timezone ID string (e.g., "Asia/Jakarta")
    private List<TimeSlotCommand> timeSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotCommand {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    /**
     * Convert timezone string to ZoneId.
     */
    public ZoneId getTimezoneAsZoneId() {
        return timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();
    }
}
