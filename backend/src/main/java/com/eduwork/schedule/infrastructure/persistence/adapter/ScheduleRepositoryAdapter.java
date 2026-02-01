package com.eduwork.schedule.infrastructure.persistence.adapter;

import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.ScheduleStatus;
import com.eduwork.schedule.domain.repository.ScheduleRepository;
import com.eduwork.schedule.infrastructure.persistence.entity.ScheduleEntity;
import com.eduwork.schedule.infrastructure.persistence.mapper.ScheduleEntityMapper;
import com.eduwork.schedule.infrastructure.persistence.repository.ScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ScheduleRepository using JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepository {

    private final ScheduleJpaRepository jpaRepository;
    private final ScheduleEntityMapper mapper;

    @Override
    @Transactional
    public Schedule save(Schedule schedule) {
        log.debug("Saving schedule: {}", schedule.getId());

        ScheduleEntity entity = mapper.toEntity(schedule);
        ScheduleEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Schedule> findById(UUID id) {
        log.debug("Finding schedule by ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findByMentorId(UUID mentorId) {
        log.debug("Finding schedules by mentor ID: {}", mentorId);

        return jpaRepository.findByMentorId(mentorId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findByMentorIdAndStatus(UUID mentorId, ScheduleStatus status) {
        log.debug("Finding schedules by mentor ID {} and status {}", mentorId, status);

        ScheduleEntity.ScheduleStatusEnum entityStatus = ScheduleEntity.ScheduleStatusEnum.valueOf(status.name());

        return jpaRepository.findByMentorIdAndStatus(mentorId, entityStatus).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findPublishedSchedules(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding published schedules between {} and {}", startDate, endDate);

        return jpaRepository.findPublishedSchedulesInDateRange(startDate, endDate).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        log.info("Deleting schedule: {}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findAllPublished() {
        log.debug("Finding all published schedules");

        return jpaRepository.findAllPublished().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByMentorIdAndStatus(UUID mentorId, ScheduleStatus status) {
        ScheduleEntity.ScheduleStatusEnum entityStatus = ScheduleEntity.ScheduleStatusEnum.valueOf(status.name());

        return jpaRepository.countByMentorIdAndStatus(mentorId, entityStatus);
    }
}
