package com.eduwork.booking.presentation.controller;

import com.eduwork.booking.application.command.BookSessionCommand;
import com.eduwork.booking.application.command.CancelBookingCommand;
import com.eduwork.booking.application.command.ConfirmPaymentCommand;
import com.eduwork.booking.application.usecase.BookSessionUseCase;
import com.eduwork.booking.application.usecase.CancelBookingUseCase;
import com.eduwork.booking.application.usecase.ConfirmPaymentUseCase;
import com.eduwork.booking.application.usecase.GetMyBookingsUseCase;
import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.presentation.dto.BookSessionRequest;
import com.eduwork.booking.presentation.dto.BookSessionResponse;
import com.eduwork.booking.presentation.dto.BookingResponse;
import com.eduwork.booking.presentation.dto.ConfirmPaymentRequest;
import com.eduwork.common.domain.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for booking operations.
 * 
 * Endpoints:
 * - POST /bookings - Book a session
 * - PUT /bookings/{id}/confirm-payment - Confirm payment
 * - DELETE /bookings/{id} - Cancel booking
 * - GET /bookings/my - Get my bookings
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookSessionUseCase bookSessionUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetMyBookingsUseCase getMyBookingsUseCase;

    /**
     * Book a session.
     * 
     * @param request   Booking request
     * @param studentId Authenticated student ID (from JWT)
     * @return Booking ID and payment instructions
     */
    @PostMapping
    @Operation(summary = "Book a session", description = "Create a booking for a session. Payment must be completed within 24 hours.")
    public ResponseEntity<BookSessionResponse> bookSession(
            @Valid @RequestBody BookSessionRequest request,
            @AuthenticationPrincipal UUID studentId) {
        log.info("Student {} booking session {}", studentId, request.sessionId());

        BookSessionCommand command = new BookSessionCommand(
                request.sessionId(),
                studentId);

        UUID bookingId = bookSessionUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookSessionResponse.success(bookingId));
    }

    /**
     * Confirm payment for a booking.
     * 
     * @param bookingId Booking ID
     * @param request   Payment confirmation request
     * @param studentId Authenticated student ID
     * @return Success message
     */
    @PutMapping("/{bookingId}/confirm-payment")
    @Operation(summary = "Confirm payment", description = "Confirm that payment has been made for a booking")
    public ResponseEntity<String> confirmPayment(
            @PathVariable UUID bookingId,
            @Valid @RequestBody ConfirmPaymentRequest request,
            @AuthenticationPrincipal UUID studentId) {
        log.info("Confirming payment for booking {}", bookingId);

        ConfirmPaymentCommand command = new ConfirmPaymentCommand(
                bookingId,
                request.paymentReference());

        confirmPaymentUseCase.execute(command);

        return ResponseEntity.ok("Payment confirmed successfully");
    }

    /**
     * Cancel a booking.
     * 
     * @param bookingId Booking ID
     * @param studentId Authenticated student ID
     * @return Refund amount
     */
    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancel a booking with automatic refund calculation")
    public ResponseEntity<String> cancelBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UUID studentId) {
        log.info("Student {} cancelling booking {}", studentId, bookingId);

        CancelBookingCommand command = new CancelBookingCommand(
                bookingId,
                studentId,
                "Student requested cancellation");

        Money refundAmount = cancelBookingUseCase.execute(command);

        String message = refundAmount.isPositive()
                ? String.format("Booking cancelled. Refund of %s will be processed.", refundAmount.format())
                : "Booking cancelled. No refund applicable.";

        return ResponseEntity.ok(message);
    }

    /**
     * Get my bookings.
     * 
     * @param studentId Authenticated student ID
     * @return List of bookings
     */
    @GetMapping("/my")
    @Operation(summary = "Get my bookings", description = "Get all bookings for the authenticated student")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UUID studentId) {
        log.info("Getting bookings for student {}", studentId);

        List<Booking> bookings = getMyBookingsUseCase.execute(studentId);

        List<BookingResponse> response = bookings.stream()
                .map(BookingResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
