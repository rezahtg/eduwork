package com.eduwork.schedule.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for TimeSlot.
 */
@Entity
@Table(name = "schedule_time_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TimeSlotStatusEnum status;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    // Enum for JPA
    public enum TimeSlotStatusEnum {
        AVAILABLE, BOOKED, CANCELLED
    }
}
