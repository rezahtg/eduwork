package com.eduwork.schedule.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for Schedule.
 */
@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ScheduleTypeEnum type;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ScheduleStatusEnum status;

    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeSlotEntity> timeSlots = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Helper methods for bidirectional relationship
    public void addTimeSlot(TimeSlotEntity timeSlot) {
        timeSlots.add(timeSlot);
        timeSlot.setSchedule(this);
    }

    public void removeTimeSlot(TimeSlotEntity timeSlot) {
        timeSlots.remove(timeSlot);
        timeSlot.setSchedule(null);
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums as inner classes for JPA
    public enum ScheduleTypeEnum {
        ONE_TIME, RECURRING
    }

    public enum ScheduleStatusEnum {
        DRAFT, PUBLISHED, CANCELLED
    }
}
