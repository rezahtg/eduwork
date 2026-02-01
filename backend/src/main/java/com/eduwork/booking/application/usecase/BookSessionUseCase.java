package com.eduwork.booking.application.usecase;

import com.eduwork.booking.application.command.BookSessionCommand;
import com.eduwork.booking.domain.exception.DuplicateBookingException;
import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.repository.BookingRepository;
import com.eduwork.common.domain.Money;
import com.eduwork.schedule.domain.exception.SessionFullException;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for booking a session.
 * 
 * Business Flow:
 * 1. Load session (with optimistic lock)
 * 2. Call session.book() which:
 * - Checks availability
 * - Checks for duplicates
 * - Creates booking
 * - Increments currentEnrollment
 * 3. Save session (version check prevents race condition)
 * 4. Save booking
 * 5. Return booking ID
 * 
 * Concurrency Handling:
 * - Optimistic locking on Session prevents overbooking
 * - If OptimisticLockException thrown, client should retry
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookSessionUseCase {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;

    /**
     * Book a session for a student.
     * 
     * @param command Booking command with session and student IDs
     * @return Booking ID
     * @throws SessionFullException      if session is full or not accepting
     *                                   bookings
     * @throws DuplicateBookingException if student already booked this session
     * @throws IllegalArgumentException  if session not found
     * @throws OptimisticLockException   if concurrent booking conflict (client
     *                                   should retry)
     */
    @Transactional
    public UUID execute(BookSessionCommand command) {
        log.info("Booking session {} for student {}", command.sessionId(), command.studentId());

        // 1. Load session
        Session session = sessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + command.sessionId()));

        // 2. Get price from session
        Money amount = session.getPricePerStudent();

        // 3. Book session (domain logic with validation)
        Booking booking = session.book(command.studentId(), amount);

        // 4. Save session (optimistic lock check happens here)
        sessionRepository.save(session);

        // 5. Save booking
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Successfully booked session {} for student {}, booking ID: {}",
                command.sessionId(), command.studentId(), savedBooking.getId());

        return savedBooking.getId();
    }
}
