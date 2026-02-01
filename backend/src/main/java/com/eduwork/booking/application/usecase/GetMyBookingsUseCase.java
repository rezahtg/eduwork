package com.eduwork.booking.application.usecase;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for getting a student's bookings.
 * Returns all bookings for a student, ordered by booking date (newest first).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetMyBookingsUseCase {

    private final BookingRepository bookingRepository;

    /**
     * Get all bookings for a student.
     * 
     * @param studentId Student ID
     * @return List of bookings (newest first)
     */
    @Transactional(readOnly = true)
    public List<Booking> execute(UUID studentId) {
        log.debug("Getting bookings for student {}", studentId);

        List<Booking> bookings = bookingRepository.findByStudentId(studentId);

        log.info("Found {} bookings for student {}", bookings.size(), studentId);

        return bookings;
    }
}
