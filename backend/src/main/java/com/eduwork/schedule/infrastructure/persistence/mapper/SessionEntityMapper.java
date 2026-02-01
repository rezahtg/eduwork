package com.eduwork.schedule.infrastructure.persistence.mapper;

import com.eduwork.common.domain.Money;
import com.eduwork.schedule.domain.model.Capacity;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.infrastructure.persistence.entity.SessionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Session domain model and SessionEntity JPA entity.
 * Handles bidirectional mapping with proper value object conversion.
 */
@Component
public class SessionEntityMapper {

    /**
     * Convert SessionEntity to Session domain model.
     */
    public Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }

        // Create capacity value object
        Capacity capacity = new Capacity(
                entity.getMinStudents(),
                entity.getMaxStudents());

        // Create money value object
        Money pricePerStudent = Money.of(
                entity.getPriceAmount(),
                entity.getCurrency());

        // Create Session using factory
        Session session = Session.create(
                entity.getScheduleId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getSessionType(),
                capacity,
                pricePerStudent);

        // Set ID and audit fields using reflection or setter
        try {
            var idField = Session.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, entity.getId());

            var statusField = Session.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(session, entity.getStatus());

            var enrollmentField = Session.class.getDeclaredField("currentEnrollment");
            enrollmentField.setAccessible(true);
            enrollmentField.set(session, entity.getCurrentEnrollment());

            var versionField = Session.class.getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.set(session, entity.getVersion());

            var createdAtField = Session.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(session, entity.getCreatedAt());

            var updatedAtField = Session.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(session, entity.getUpdatedAt());

            if (entity.getConfirmedAt() != null) {
                var confirmedAtField = Session.class.getDeclaredField("confirmedAt");
                confirmedAtField.setAccessible(true);
                confirmedAtField.set(session, entity.getConfirmedAt());
            }

            if (entity.getStartedAt() != null) {
                var startedAtField = Session.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);
                startedAtField.set(session, entity.getStartedAt());
            }

            if (entity.getCompletedAt() != null) {
                var completedAtField = Session.class.getDeclaredField("completedAt");
                completedAtField.setAccessible(true);
                completedAtField.set(session, entity.getCompletedAt());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to map SessionEntity to Session", e);
        }

        return session;
    }

    /**
     * Convert Session domain model to SessionEntity.
     */
    public SessionEntity toEntity(Session session) {
        if (session == null) {
            return null;
        }

        SessionEntity entity = new SessionEntity();
        entity.setId(session.getId());
        entity.setScheduleId(session.getScheduleId());
        entity.setStartTime(session.getStartTime());
        entity.setEndTime(session.getEndTime());
        entity.setSessionType(session.getSessionType());
        entity.setPriceAmount(session.getPricePerStudent().getAmount());
        entity.setCurrency(session.getPricePerStudent().getCurrency());
        entity.setMinStudents(session.getCapacity().minStudents());
        entity.setMaxStudents(session.getCapacity().maxStudents());
        entity.setStatus(session.getStatus());
        entity.setCurrentEnrollment(session.getCurrentEnrollment());
        entity.setVersion(session.getVersion());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        entity.setConfirmedAt(session.getConfirmedAt());
        entity.setStartedAt(session.getStartedAt());
        entity.setCompletedAt(session.getCompletedAt());

        return entity;
    }

    /**
     * Update existing entity from domain model (for updates).
     */
    public void updateEntity(Session session, SessionEntity entity) {
        entity.setStatus(session.getStatus());
        entity.setCurrentEnrollment(session.getCurrentEnrollment());
        entity.setUpdatedAt(session.getUpdatedAt());
        entity.setConfirmedAt(session.getConfirmedAt());
        entity.setStartedAt(session.getStartedAt());
        entity.setCompletedAt(session.getCompletedAt());
        // Note: version is managed by JPA @Version, don't set manually
    }
}
