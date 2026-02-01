package com.eduwork.booking.infrastructure.persistence.adapter;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.model.BookingStatus;
import com.eduwork.booking.domain.repository.BookingRepository;
import com.eduwork.booking.infrastructure.persistence.entity.BookingEntity;
import com.eduwork.booking.infrastructure.persistence.mapper.BookingEntityMapper;
import com.eduwork.booking.infrastructure.persistence.repository.JpaBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of BookingRepository using JPA.
 */
@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final JpaBookingRepository jpaRepository;
    private final BookingEntityMapper mapper;

    @Override
    @Transactional
    public Booking save(Booking booking) {
        BookingEntity entity = mapper.toEntity(booking);
        BookingEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBySessionId(UUID sessionId) {
        return jpaRepository.findBySessionIdOrderByBookedAtAsc(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByStudentId(UUID studentId) {
        return jpaRepository.findByStudentIdOrderByBookedAtDesc(studentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findBySessionIdAndStudentId(UUID sessionId, UUID studentId) {
        return jpaRepository.findBySessionIdAndStudentId(sessionId, studentId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByStatus(BookingStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findExpiredUnpaidBookings(LocalDateTime now) {
        return jpaRepository.findExpiredUnpaidBookings(now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveBookingsForSession(UUID sessionId) {
        return jpaRepository.countActiveBookingsForSession(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findActiveBookingsForSession(UUID sessionId) {
        return jpaRepository.findActiveBookingsForSession(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Booking booking) {
        jpaRepository.deleteById(booking.getId());
    }
}
