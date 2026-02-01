package com.eduwork.booking.infrastructure.persistence.entity;

import com.eduwork.booking.domain.model.BookingStatus;
import com.eduwork.booking.domain.model.CancellationReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Booking aggregate.
 * Maps to 'bookings' table with platform fee tracking.
 */
@Entity
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(name = "uk_session_student", columnNames = {
        "session_id", "student_id" }), indexes = {
                @Index(name = "idx_bookings_session_id", columnList = "session_id"),
                @Index(name = "idx_bookings_student_id", columnList = "student_id"),
                @Index(name = "idx_bookings_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    // Financial (platform fee model: 15% platform, 85% mentor)
    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "platform_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "mentor_payout", nullable = false, precision = 12, scale = 2)
    private BigDecimal mentorPayout;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "IDR";

    // Payment
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "payment_deadline", nullable = false)
    private LocalDateTime paymentDeadline;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    // Refund
    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 50)
    private CancellationReason cancellationReason;

    // Timestamps
    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (bookedAt == null) {
            bookedAt = LocalDateTime.now();
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
