package com.eduwork.booking.infrastructure.persistence.repository;

import com.eduwork.booking.domain.model.BookingStatus;
import com.eduwork.booking.infrastructure.persistence.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for BookingEntity.
 * Provides query methods for booking management and payment tracking.
 */
@Repository
public interface JpaBookingRepository extends JpaRepository<BookingEntity, UUID> {

    /**
     * Find all bookings for a specific session.
     */
    List<BookingEntity> findBySessionIdOrderByBookedAtAsc(UUID sessionId);

    /**
     * Find all bookings for a specific student.
     */
    List<BookingEntity> findByStudentIdOrderByBookedAtDesc(UUID studentId);

    /**
     * Find booking by session and student.
     */
    Optional<BookingEntity> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    /**
     * Find bookings by status.
     */
    List<BookingEntity> findByStatus(BookingStatus status);

    /**
     * Find expired unpaid bookings (past payment deadline).
     * Used by scheduled job to auto-cancel.
     */
    @Query("""
            SELECT b FROM BookingEntity b
            WHERE b.status = 'PENDING_PAYMENT'
            AND b.paymentDeadline < :now
            """)
    List<BookingEntity> findExpiredUnpaidBookings(@Param("now") LocalDateTime now);

    /**
     * Count active bookings for a session (for minimum check).
     */
    @Query("""
            SELECT COUNT(b) FROM BookingEntity b
            WHERE b.sessionId = :sessionId
            AND b.status IN ('PAID', 'CONFIRMED', 'IN_SESSION', 'COMPLETED')
            """)
    long countActiveBookingsForSession(@Param("sessionId") UUID sessionId);

    /**
     * Find all active bookings for a session.
     */
    @Query("""
            SELECT b FROM BookingEntity b
            WHERE b.sessionId = :sessionId
            AND b.status IN ('PAID', 'CONFIRMED', 'IN_SESSION')
            ORDER BY b.bookedAt ASC
            """)
    List<BookingEntity> findActiveBookingsForSession(@Param("sessionId") UUID sessionId);
}
