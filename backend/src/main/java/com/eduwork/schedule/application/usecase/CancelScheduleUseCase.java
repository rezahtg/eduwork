package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
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
 * Use case for cancelling a schedule and all associated sessions.
 * 
 * Business Rules:
 * - Only mentor who owns the schedule can cancel it
 * - Cancels all associated sessions
 * - Sessions handle their own booking cancellations with full refunds
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final SessionRepository sessionRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleResponseDTO execute(UUID scheduleId, UUID mentorId) {
        log.info("Cancelling schedule: {}", scheduleId);

        // Fetch schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleValidationException("Schedule not found"));

        // Authorization check
        if (!schedule.getMentorId().equals(mentorId)) {
            throw new ScheduleValidationException("Not authorized to cancel this schedule");
        }

        // Cancel schedule
        schedule.cancel();

        // Find and cancel all sessions for this schedule
        List<Session> sessions = sessionRepository.findByScheduleId(scheduleId);
        int canceledSessionCount = 0;

        for (Session session : sessions) {
            if (!session.getStatus().isTerminal()) {
                session.cancel(); // Cancels all bookings with full refunds
                sessionRepository.save(session);
                canceledSessionCount++;
                log.debug("Canceled session: {} (status: {})",
                        session.getId(), session.getStatus());
            }
        }

        // Save schedule
        Schedule cancelled = scheduleRepository.save(schedule);

        log.info("Schedule cancelled successfully: {} ({} sessions canceled)",
                cancelled.getId(), canceledSessionCount);

        return scheduleMapper.toResponseDTO(cancelled);
    }
}
