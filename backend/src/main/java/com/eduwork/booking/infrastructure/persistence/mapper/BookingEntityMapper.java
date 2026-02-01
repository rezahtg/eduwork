package com.eduwork.booking.infrastructure.persistence.mapper;

import com.eduwork.booking.domain.model.Booking;
import com.eduwork.booking.infrastructure.persistence.entity.BookingEntity;
import com.eduwork.common.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mapper between Booking domain model and BookingEntity JPA entity.
 */
@Component
public class BookingEntityMapper {

    /**
     * Convert BookingEntity to Booking domain model.
     */
    public Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }

        // Create money value objects
        Money amountPaid = Money.of(entity.getAmountPaid(), entity.getCurrency());

        // Create Booking using factory
        Booking booking = Booking.create(
                entity.getSessionId(),
                entity.getStudentId(),
                amountPaid,
                entity.getPaymentDeadline());

        // Set fields using reflection
        try {
            var idField = Booking.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(booking, entity.getId());

            var statusField = Booking.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(booking, entity.getStatus());

            var platformFeeField = Booking.class.getDeclaredField("platformFee");
            platformFeeField.setAccessible(true);
            platformFeeField.set(booking, Money.of(entity.getPlatformFee(), entity.getCurrency()));

            var mentorPayoutField = Booking.class.getDeclaredField("mentorPayout");
            mentorPayoutField.setAccessible(true);
            mentorPayoutField.set(booking, Money.of(entity.getMentorPayout(), entity.getCurrency()));

            if (entity.getPaymentReference() != null) {
                var paymentRefField = Booking.class.getDeclaredField("paymentReference");
                paymentRefField.setAccessible(true);
                paymentRefField.set(booking, entity.getPaymentReference());
            }

            if (entity.getRefundAmount() != null) {
                var refundAmountField = Booking.class.getDeclaredField("refundAmount");
                refundAmountField.setAccessible(true);
                refundAmountField.set(booking, Money.of(entity.getRefundAmount(), entity.getCurrency()));
            }

            if (entity.getCancellationReason() != null) {
                var reasonField = Booking.class.getDeclaredField("cancellationReason");
                reasonField.setAccessible(true);
                reasonField.set(booking, entity.getCancellationReason());
            }

            var bookedAtField = Booking.class.getDeclaredField("bookedAt");
            bookedAtField.setAccessible(true);
            bookedAtField.set(booking, entity.getBookedAt());

            if (entity.getPaidAt() != null) {
                var paidAtField = Booking.class.getDeclaredField("paidAt");
                paidAtField.setAccessible(true);
                paidAtField.set(booking, entity.getPaidAt());
            }

            if (entity.getConfirmedAt() != null) {
                var confirmedAtField = Booking.class.getDeclaredField("confirmedAt");
                confirmedAtField.setAccessible(true);
                confirmedAtField.set(booking, entity.getConfirmedAt());
            }

            if (entity.getCancelledAt() != null) {
                var cancelledAtField = Booking.class.getDeclaredField("cancelledAt");
                cancelledAtField.setAccessible(true);
                cancelledAtField.set(booking, entity.getCancelledAt());
            }

            if (entity.getRefundedAt() != null) {
                var refundedAtField = Booking.class.getDeclaredField("refundedAt");
                refundedAtField.setAccessible(true);
                refundedAtField.set(booking, entity.getRefundedAt());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to map BookingEntity to Booking", e);
        }

        return booking;
    }

    /**
     * Convert Booking domain model to BookingEntity.
     */
    public BookingEntity toEntity(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingEntity entity = new BookingEntity();
        entity.setId(booking.getId());
        entity.setSessionId(booking.getSessionId());
        entity.setStudentId(booking.getStudentId());
        entity.setAmountPaid(booking.getAmountPaid().getAmount());
        entity.setPlatformFee(booking.getPlatformFee().getAmount());
        entity.setMentorPayout(booking.getMentorPayout().getAmount());
        entity.setCurrency(booking.getAmountPaid().getCurrency());
        entity.setStatus(booking.getStatus());
        entity.setPaymentDeadline(booking.getPaymentDeadline());
        entity.setPaymentReference(booking.getPaymentReference());

        if (booking.getRefundAmount() != null) {
            entity.setRefundAmount(booking.getRefundAmount().getAmount());
        }
        entity.setRefundedAt(booking.getRefundedAt());
        entity.setCancellationReason(booking.getCancellationReason());

        entity.setBookedAt(booking.getBookedAt());
        entity.setPaidAt(booking.getPaidAt());
        entity.setConfirmedAt(booking.getConfirmedAt());
        entity.setCancelledAt(booking.getCancelledAt());

        return entity;
    }

    /**
     * Update existing entity from domain model.
     */
    public void updateEntity(Booking booking, BookingEntity entity) {
        entity.setStatus(booking.getStatus());
        entity.setPaymentReference(booking.getPaymentReference());

        if (booking.getRefundAmount() != null) {
            entity.setRefundAmount(booking.getRefundAmount().getAmount());
        }
        entity.setRefundedAt(booking.getRefundedAt());
        entity.setCancellationReason(booking.getCancellationReason());

        entity.setPaidAt(booking.getPaidAt());
        entity.setConfirmedAt(booking.getConfirmedAt());
        entity.setCancelledAt(booking.getCancelledAt());
    }
}
