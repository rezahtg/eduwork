package com.eduwork.booking.application.usecase;

import com.eduwork.booking.application.command.CancelBookingCommand;
import com.eduwork.booking.domain.exception.BookingNotFoundException;
import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.model.CancellationReason;
import com.eduwork.booking.domain.repository.BookingRepository;
import com.eduwork.common.domain.Money;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for cancelling a booking with refund calculation.
 * 
 * Business Flow:
 * 1. Load booking
 * 2. Authorize (check student owns booking)
 * 3. Load session
 * 4. Cancel booking on session (calculates refund using RefundPolicy)
 * 5. Process refund if applicable
 * 6. Save both booking and session
 * 
 * Refund Policy:
 * - 7+ days: 100% refund (minus platform fee)
 * - 3-7 days: 70% refund (minus platform fee)
 * - 1-3 days: 50% refund (minus platform fee)
 * - <24h: 25% refund (minus platform fee)
 * - In progress: 0% refund (requires emergency dispute)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;

    /**
     * Cancel a booking with automatic refund calculation.
     * 
     * @param command Cancellation command
     * @return Refund amount (can be zero)
     * @throws BookingNotFoundException if booking not found
     * @throws IllegalStateException    if booking cannot be cancelled
     * @throws SecurityException        if student doesn't own the booking
     */
    @Transactional
    public Money execute(CancelBookingCommand command) {
        log.info("Cancelling booking {} for student {}", command.bookingId(), command.studentId());

        // 1. Load booking
        Booking booking = bookingRepository.findById(command.bookingId())
                .orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

        // 2. Authorization check
        if (!booking.getStudentId().equals(command.studentId())) {
            throw new SecurityException("Student does not own this booking");
        }

        // 3. Load session
        Session session = sessionRepository.findById(booking.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + booking.getSessionId()));

        // 4. Cancel booking on session (refund calculated automatically by
        // RefundPolicy)
        CancellationReason reason = command.getCancellationReason();
        session.cancelBooking(booking.getId(), reason);

        // 5. Get calculated refund amount
        Money refundAmount = booking.getRefundAmount();

        // 6. Mark refund as processed if amount > 0
        if (refundAmount != null && refundAmount.isPositive()) {
            booking.processRefund();
            log.info("Refund of {} will be processed for booking {}", refundAmount, command.bookingId());
        } else {
            log.info("No refund applicable for booking {}", command.bookingId());
        }

        // 7. Save both
        bookingRepository.save(booking);
        sessionRepository.save(session);

        log.info("Booking {} cancelled, refund: {}", command.bookingId(), refundAmount);

        return refundAmount != null ? refundAmount : Money.zero(booking.getAmountPaid().getCurrency());
    }
}
