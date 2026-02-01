package com.eduwork.schedule.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a new schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Pattern(regexp = "^[A-Z][a-z]+/[A-Z][a-z_]+$", message = "Timezone must be a valid timezone ID (e.g., Asia/Jakarta)")
    private String timezone; // Optional, defaults to system timezone

    @NotNull(message = "Time slots are required")
    @Size(min = 1, message = "At least one time slot is required")
    @Valid
    private List<TimeSlotRequest> timeSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotRequest {

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        private LocalDateTime startTime;

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        private LocalDateTime endTime;
    }
}
