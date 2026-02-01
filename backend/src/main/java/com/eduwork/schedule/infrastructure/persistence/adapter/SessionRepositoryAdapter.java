package com.eduwork.schedule.infrastructure.persistence.adapter;

import com.eduwork.schedule.domain.model.SessionStatus;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import com.eduwork.schedule.infrastructure.persistence.entity.SessionEntity;
import com.eduwork.schedule.infrastructure.persistence.mapper.SessionEntityMapper;
import com.eduwork.schedule.infrastructure.persistence.repository.JpaSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of SessionRepository using JPA.
 * Translates between domain models and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class SessionRepositoryAdapter implements SessionRepository {

    private final JpaSessionRepository jpaRepository;
    private final SessionEntityMapper mapper;

    @Override
    @Transactional
    public Session save(Session session) {
        SessionEntity entity = mapper.toEntity(session);
        SessionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findByScheduleId(UUID scheduleId) {
        return jpaRepository.findByScheduleIdOrderByStartTimeAsc(scheduleId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findByStatus(SessionStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findOpenSessionsAfter(LocalDateTime startTime) {
        return jpaRepository.findOpenSessionsAfter(startTime)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findSessionsToStart(LocalDateTime now) {
        return jpaRepository.findSessionsToStart(now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> findSessionsToComplete(LocalDateTime now) {
        return jpaRepository.findSessionsToComplete(now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Session session) {
        jpaRepository.deleteById(session.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
