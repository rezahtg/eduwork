package com.eduwork.schedule.infrastructure.persistence.mapper;

import com.eduwork.schedule.domain.model.*;
import com.eduwork.schedule.infrastructure.persistence.entity.ScheduleEntity;
import com.eduwork.schedule.infrastructure.persistence.entity.TimeSlotEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between Schedule domain model and ScheduleEntity.
 */
@Component
public class ScheduleEntityMapper {

    public ScheduleEntity toEntity(Schedule domain) {
        ScheduleEntity entity = ScheduleEntity.builder()
                .id(domain.getId())
                .mentorId(domain.getMentorId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .type(toEntityType(domain.getType()))
                .status(toEntityStatus(domain.getStatus()))
                .timezone(domain.getTimezone().getId())
                .timeSlots(new ArrayList<>())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .publishedAt(domain.getPublishedAt())
                .build();

        // Map time slots
        for (TimeSlot slot : domain.getTimeSlots()) {
            TimeSlotEntity slotEntity = toTimeSlotEntity(slot);
            entity.addTimeSlot(slotEntity);
        }

        return entity;
    }

    public Schedule toDomain(ScheduleEntity entity) {
        List<TimeSlot> timeSlots = entity.getTimeSlots().stream()
                .map(this::toTimeSlotDomain)
                .toList();

        Schedule schedule = Schedule.builder()
                .id(entity.getId())
                .mentorId(entity.getMentorId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(toDomainType(entity.getType()))
                .timezone(ZoneId.of(entity.getTimezone()))
                .timeSlots(timeSlots)
                .build();

        // Set protected fields using reflection (or add package-private setters)
        setProtectedFields(schedule, entity);

        return schedule;
    }

    private TimeSlotEntity toTimeSlotEntity(TimeSlot domain) {
        return TimeSlotEntity.builder()
                .id(domain.getId())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .status(toTimeSlotEntityStatus(domain.getStatus()))
                .bookingId(domain.getBookingId())
                .bookedAt(domain.getBookedAt())
                .cancelledAt(domain.getCancelledAt())
                .build();
    }

    private TimeSlot toTimeSlotDomain(TimeSlotEntity entity) {
        TimeSlot slot = new TimeSlot(
                entity.getId(),
                entity.getStartTime(),
                entity.getEndTime(),
                toTimeSlotDomainStatus(entity.getStatus()));

        // Set protected fields
        setTimeSlotProtectedFields(slot, entity);

        return slot;
    }

    // Enum conversions
    private ScheduleEntity.ScheduleTypeEnum toEntityType(ScheduleType domain) {
        return ScheduleEntity.ScheduleTypeEnum.valueOf(domain.name());
    }

    private ScheduleType toDomainType(ScheduleEntity.ScheduleTypeEnum entity) {
        return ScheduleType.valueOf(entity.name());
    }

    private ScheduleEntity.ScheduleStatusEnum toEntityStatus(ScheduleStatus domain) {
        return ScheduleEntity.ScheduleStatusEnum.valueOf(domain.name());
    }

    private ScheduleStatus toDomainStatus(ScheduleEntity.ScheduleStatusEnum entity) {
        return ScheduleStatus.valueOf(entity.name());
    }

    private TimeSlotEntity.TimeSlotStatusEnum toTimeSlotEntityStatus(TimeSlotStatus domain) {
        return TimeSlotEntity.TimeSlotStatusEnum.valueOf(domain.name());
    }

    private TimeSlotStatus toTimeSlotDomainStatus(TimeSlotEntity.TimeSlotStatusEnum entity) {
        return TimeSlotStatus.valueOf(entity.name());
    }

    // Helper methods to set protected fields using reflection
    private void setProtectedFields(Schedule schedule, ScheduleEntity entity) {
        try {
            setField(schedule, "status", toDomainStatus(entity.getStatus()));
            setField(schedule, "createdAt", entity.getCreatedAt());
            setField(schedule, "updatedAt", entity.getUpdatedAt());
            setField(schedule, "publishedAt", entity.getPublishedAt());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set protected fields on Schedule", e);
        }
    }

    private void setTimeSlotProtectedFields(TimeSlot slot, TimeSlotEntity entity) {
        try {
            setField(slot, "bookingId", entity.getBookingId());
            setField(slot, "bookedAt", entity.getBookedAt());
            setField(slot, "cancelledAt", entity.getCancelledAt());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set protected fields on TimeSlot", e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
