package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.command.PublishScheduleCommand;
import com.eduwork.schedule.application.dto.ScheduleWithSessionsResponseDTO;
import com.eduwork.schedule.application.dto.SessionSummaryDTO;
import com.eduwork.schedule.domain.exception.ScheduleNotFoundException;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for publishing a schedule and generating bookable sessions.
 * 
 * Business Rules:
 * - Only mentor who owns the schedule can publish it
 * - Schedule must be in DRAFT status
 * - Generates Session aggregates from TimeSlots
 * - Sessions are automatically published (DRAFT → OPEN)
 * - Transactional: Either all sessions are created or none
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublishScheduleWithSessionsUseCase {

    private final ScheduleRepository scheduleRepository;
    private final SessionRepository sessionRepository;

    /**
     * Execute the use case to publish a schedule and generate sessions.
     * 
     * @param command The publish schedule command
     * @return Response containing schedule and generated sessions
     * @throws ScheduleNotFoundException   if schedule doesn't exist
     * @throws ScheduleValidationException if validation fails or unauthorized
     */
    @Transactional
    public ScheduleWithSessionsResponseDTO execute(PublishScheduleCommand command) {
        UUID scheduleId = command.getScheduleId();
        UUID mentorId = command.getMentorId();

        log.info("Publishing schedule {} for mentor {}", scheduleId, mentorId);

        // 1. Load schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(
                        "Schedule not found: " + scheduleId));

        // 2. Authorization check
        if (!schedule.getMentorId().equals(mentorId)) {
            throw new ScheduleValidationException(
                    "Not authorized to publish this schedule");
        }

        // 3. Publish schedule (generates sessions from time slots)
        List<Session> generatedSessions = schedule.publish();

        log.info("Generated {} sessions from schedule {}", generatedSessions.size(), scheduleId);

        // 4. Save schedule (now in PUBLISHED status)
        Schedule publishedSchedule = scheduleRepository.save(schedule);

        // 5. Save all generated sessions
        generatedSessions.forEach(session -> {
            sessionRepository.save(session);
            log.debug("Saved session: {}", session.getId());
        });

        log.info("Successfully published schedule {} with {} sessions",
                scheduleId, generatedSessions.size());

        // 6. Build response
        List<SessionSummaryDTO> sessionSummaries = generatedSessions.stream()
                .map(session -> SessionSummaryDTO.builder()
                        .id(session.getId())
                        .startTime(session.getStartTime())
                        .endTime(session.getEndTime())
                        .status(session.getStatus().name())
                        .sessionType(session.getSessionType().name())
                        .availableSlots(session.getAvailableSlots())
                        .currentEnrollment(session.getCurrentEnrollment())
                        .build())
                .toList();

        return ScheduleWithSessionsResponseDTO.builder()
                .scheduleId(publishedSchedule.getId())
                .title(publishedSchedule.getTitle())
                .status(publishedSchedule.getStatus().name())
                .sessionsCreated(generatedSessions.size())
                .sessions(sessionSummaries)
                .build();
    }
}
