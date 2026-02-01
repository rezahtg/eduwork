package com.eduwork.booking.application.usecase;

import com.eduwork.booking.application.command.ConfirmPaymentCommand;
import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.repository.BookingRepository;
import com.eduwork.booking.domain.exception.BookingNotFoundException;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for confirming payment of a booking.
 * 
 * Business Flow:
 * 1. Load booking
 * 2. Confirm payment on booking (PENDING_PAYMENT → PAID)
 * 3. Set payment reference from gateway
 * 4. Load session
 * 5. Confirm booking on session (checks minimum, updates status)
 * 6. Save both booking and session
 * 
 * Status Transitions:
 * - Booking: PENDING_PAYMENT → PAID → CONFIRMED (when session confirmed)
 * - Session: OPEN → WAITING → CONFIRMED (when minimum reached)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmPaymentUseCase {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;

    /**
     * Confirm payment for a booking.
     * 
     * @param command Payment confirmation command
     * @throws BookingNotFoundException if booking not found
     * @throws IllegalStateException    if booking not in PENDING_PAYMENT status
     */
    @Transactional
    public void execute(ConfirmPaymentCommand command) {
        log.info("Confirming payment for booking {}", command.bookingId());

        // 1. Load booking
        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

        // 2. Confirm payment (domain validation)
        booking.confirmPayment();
        booking.setPaymentReference(command.paymentReference());

        // 3. Load session
        Session session = sessionRepository.findById(booking.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + booking.getSessionId()));

        // 4. Confirm booking on session (may trigger session confirmation if minimum
        // reached)
        session.confirmBooking(booking.getId());

        // 5. Save both
        bookingRepository.save(booking);
        sessionRepository.save(session);

        log.info("Payment confirmed for booking {}, session status: {}",
                command.bookingId(), session.getStatus());
    }
}
