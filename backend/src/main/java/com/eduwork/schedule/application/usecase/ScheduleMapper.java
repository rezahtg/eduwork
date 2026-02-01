package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.application.dto.ScheduleResponseDTO;
import com.eduwork.schedule.domain.model.Schedule;
import com.eduwork.schedule.domain.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Schedule entities to DTOs.
 */
@Component
public class ScheduleMapper {

    public ScheduleResponseDTO toResponseDTO(Schedule schedule) {
        return ScheduleResponseDTO.builder()
                .id(schedule.getId())
                .mentorId(schedule.getMentorId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .type(schedule.getType())
                .status(schedule.getStatus())
                .timezone(schedule.getTimezone().getId())
                .timeSlots(toTimeSlotDTOs(schedule.getTimeSlots()))
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .publishedAt(schedule.getPublishedAt())
                .build();
    }

    public List<ScheduleResponseDTO.TimeSlotDTO> toTimeSlotDTOs(List<TimeSlot> timeSlots) {
        return timeSlots.stream()
                .map(this::toTimeSlotDTO)
                .toList();
    }

    public ScheduleResponseDTO.TimeSlotDTO toTimeSlotDTO(TimeSlot timeSlot) {
        return ScheduleResponseDTO.TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .status(timeSlot.getStatus().name())
                .bookingId(timeSlot.getBookingId())
                .durationMinutes(timeSlot.getDurationMinutes())
                .build();
    }
}
