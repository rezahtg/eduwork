package com.eduwork.booking.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for confirming payment.
 */
public record ConfirmPaymentRequest(
        @NotBlank(message = "Payment reference is required") String paymentReference) {
}
