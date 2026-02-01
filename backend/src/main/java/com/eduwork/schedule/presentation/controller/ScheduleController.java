package com.eduwork.schedule.presentation.controller;

import com.eduwork.schedule.application.command.CreateScheduleCommand;
import com.eduwork.schedule.application.command.UpdateScheduleCommand;
import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.application.usecase.*;
import com.eduwork.schedule.presentation.dto.CreateScheduleRequest;
import com.eduwork.schedule.presentation.dto.UpdateScheduleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for mentor schedule management.
 * Endpoints:
 * - POST /api/v1/schedules - Create a new schedule
 * - PUT /api/v1/schedules/{id} - Update a schedule
 * - POST /api/v1/schedules/{id}/publish - Publish a schedule
 * - POST /api/v1/schedules/{id}/cancel - Cancel a schedule
 * - GET /api/v1/schedules/my - Get my schedules
 * - GET /api/v1/schedules/{id} - Get schedule details
 */
@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

        private final CreateScheduleUseCase createScheduleUseCase;
        private final UpdateScheduleUseCase updateScheduleUseCase;
        private final PublishScheduleUseCase publishScheduleUseCase;
        private final CancelScheduleUseCase cancelScheduleUseCase;
        private final GetMySchedulesUseCase getMySchedulesUseCase;
        private final GetScheduleDetailsUseCase getScheduleDetailsUseCase;

        /**
         * Create a new schedule (DRAFT status).
         */
        @PostMapping()
        public ResponseEntity<ScheduleResponseDTO> createSchedule(
                        @Valid @RequestBody CreateScheduleRequest request,
                        @AuthenticationPrincipal String userId) {
                log.info("Creating schedule for user: {}", userId);

                UUID mentorId = UUID.fromString(userId);

                CreateScheduleCommand command = CreateScheduleCommand.builder()
                                .mentorId(mentorId)
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .timezone(request.getTimezone())
                                .timeSlots(request.getTimeSlots().stream()
                                                .map(slot -> CreateScheduleCommand.TimeSlotCommand.builder()
                                                                .startTime(slot.getStartTime())
                                                                .endTime(slot.getEndTime())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();

                ScheduleResponseDTO response = createScheduleUseCase.execute(command);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Update an existing schedule (DRAFT only).
         */
        @PutMapping("/{scheduleId}")
        public ResponseEntity<ScheduleResponseDTO> updateSchedule(
                        @PathVariable UUID scheduleId,
                        @Valid @RequestBody UpdateScheduleRequest request,
                        @AuthenticationPrincipal String userId) {
                log.info("Updating schedule: {}", scheduleId);

                UUID mentorId = UUID.fromString(userId);

                UpdateScheduleCommand command = UpdateScheduleCommand.builder()
                                .scheduleId(scheduleId)
                                .mentorId(mentorId)
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .timezone(request.getTimezone())
                                .timeSlotUpdates(request.getTimeSlotUpdates() != null
                                                ? request.getTimeSlotUpdates().stream()
                                                                .map(slot -> UpdateScheduleCommand.TimeSlotUpdate
                                                                                .builder()
                                                                                .slotId(slot.getSlotId())
                                                                                .startTime(slot.getStartTime())
                                                                                .endTime(slot.getEndTime())
                                                                                .delete(slot.isDelete())
                                                                                .build())
                                                                .collect(Collectors.toList())
                                                : null)
                                .build();

                ScheduleResponseDTO response = updateScheduleUseCase.execute(command);

                return ResponseEntity.ok(response);
        }

        /**
         * Publish a schedule (make it available for bookings).
         */
        @PostMapping("/{scheduleId}/publish")
        public ResponseEntity<ScheduleResponseDTO> publishSchedule(
                        @PathVariable UUID scheduleId,
                        @AuthenticationPrincipal String userId) {
                log.info("Publishing schedule: {}", scheduleId);

                UUID mentorId = UUID.fromString(userId);
                ScheduleResponseDTO response = publishScheduleUseCase.execute(scheduleId, mentorId);

                return ResponseEntity.ok(response);
        }

        /**
         * Cancel a schedule.
         */
        @PostMapping("/{scheduleId}/cancel")
        public ResponseEntity<ScheduleResponseDTO> cancelSchedule(
                        @PathVariable UUID scheduleId,
                        @AuthenticationPrincipal String userId) {
                log.info("Cancelling schedule: {}", scheduleId);

                UUID mentorId = UUID.fromString(userId);
                ScheduleResponseDTO response = cancelScheduleUseCase.execute(scheduleId, mentorId);

                return ResponseEntity.ok(response);
        }

        /**
         * Get all my schedules (mentor view).
         */
        @GetMapping("/my")
        public ResponseEntity<List<ScheduleResponseDTO>> getMySchedules(
                        @AuthenticationPrincipal String userId) {
                log.info("Getting schedules for user: {}", userId);

                UUID mentorId = UUID.fromString(userId);
                List<ScheduleResponseDTO> schedules = getMySchedulesUseCase.execute(mentorId);

                return ResponseEntity.ok(schedules);
        }

        /**
         * Get schedule details by ID.
         */
        @GetMapping("/{scheduleId}")
        public ResponseEntity<ScheduleResponseDTO> getScheduleDetails(
                        @PathVariable UUID scheduleId) {
                log.info("Getting schedule details: {}", scheduleId);

                ScheduleResponseDTO response = getScheduleDetailsUseCase.execute(scheduleId);

                return ResponseEntity.ok(response);
        }
}
