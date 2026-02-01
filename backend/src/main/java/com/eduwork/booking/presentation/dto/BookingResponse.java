package com.eduwork.booking.presentation.dto;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for booking details.
 */
public record BookingResponse(
        UUID id,
        UUID sessionId,
        UUID studentId,
        BigDecimal amountPaid,
        BigDecimal platformFee,
        BigDecimal mentorPayout,
        String currency,
        BookingStatus status,
        LocalDateTime paymentDeadline,
        String paymentReference,
        BigDecimal refundAmount,
        LocalDateTime bookedAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getSessionId(),
                booking.getStudentId(),
                booking.getAmountPaid().getAmount(),
                booking.getPlatformFee().getAmount(),
                booking.getMentorPayout().getAmount(),
                booking.getAmountPaid().getCurrency(),
                booking.getStatus(),
                booking.getPaymentDeadline(),
                booking.getPaymentReference(),
                booking.getRefundAmount() != null ? booking.getRefundAmount().getAmount() : null,
                booking.getBookedAt(),
                booking.getPaidAt(),
                booking.getCancelledAt());
    }
}
