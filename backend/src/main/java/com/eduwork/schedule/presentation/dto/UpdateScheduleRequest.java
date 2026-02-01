package com.eduwork.schedule.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for updating an existing schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {

    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Pattern(regexp = "^[A-Z][a-z]+/[A-Z][a-z_]+$", message = "Timezone must be a valid timezone ID (e.g., Asia/Jakarta)")
    private String timezone;

    @Valid
    private List<TimeSlotUpdateRequest> timeSlotUpdates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotUpdateRequest {

        private UUID slotId; // null for new slots
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean delete; // true to delete this slot
    }
}
