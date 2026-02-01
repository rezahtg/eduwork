package com.eduwork.booking.domain.repository;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Booking aggregate.
 */
public interface BookingRepository {

    /**
     * Save a new booking or update existing.
     */
    Booking save(Booking booking);

    /**
     * Find booking by ID.
     */
    Optional<Booking> findById(UUID id);

    /**
     * Find all bookings for a session.
     */
    List<Booking> findBySessionId(UUID sessionId);

    /**
     * Find all bookings for a student.
     */
    List<Booking> findByStudentId(UUID studentId);

    /**
     * Find booking by session and student.
     */
    Optional<Booking> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    /**
     * Find bookings by status.
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Find expired unpaid bookings (past payment deadline).
     */
    List<Booking> findExpiredUnpaidBookings(LocalDateTime now);

    /**
     * Count active bookings for a session.
     */
    long countActiveBookingsForSession(UUID sessionId);

    /**
     * Find active bookings for a session.
     */
    List<Booking> findActiveBookingsForSession(UUID sessionId);

    /**
     * Delete booking.
     */
    void delete(Booking booking);
}
