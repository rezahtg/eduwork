package com.eduwork.booking.domain.model;

import com.eduwork.common.domain.Money;

import java.time.Duration;

/**
 * RefundPolicy calculates refund amounts based on cancellation timing.
 * 
 * Business Rules (Eduwork Platform):
 * - Platform fee: 15% of booking (non-refundable)
 * - Refund tiers (applied to amount after platform fee):
 * - 7+ days before: 100% refund
 * - 3-7 days: 70% refund
 * - 1-3 days: 50% refund
 * - <24 hours: 25% refund
 * - In progress/past: 0% refund
 * 
 * Example:
 * - Student paid: 150,000 IDR
 * - Platform fee: 22,500 IDR (15%)
 * - Refundable base: 127,500 IDR
 * - Cancelled 5 days before → 70% of 127,500 = 89,250 IDR refund
 * 
 * Immutable value object.
 */
public record RefundPolicy(
        double platformFeePercentage,
        RefundTier weekBefore,
        RefundTier threeDays,
        RefundTier oneDay,
        RefundTier lessThanDay) {
    /**
     * Standard Eduwork refund policy.
     * Platform fee 15%, Fair refund tiers.
     */
    public static RefundPolicy standard() {
        return new RefundPolicy(
                0.15, // 15% platform fee
                new RefundTier(Duration.ofDays(7), 1.00), // 100% refund (7+ days)
                new RefundTier(Duration.ofDays(3), 0.70), // 70% refund (3-7 days)
                new RefundTier(Duration.ofDays(1), 0.50), // 50% refund (1-3 days)
                new RefundTier(Duration.ofHours(1), 0.25) // 25% refund (<24h)
        );
    }

    /**
     * Calculate refund amount based on cancellation timing.
     * 
     * @param originalAmount   Amount student paid
     * @param timeUntilSession Time remaining until session starts
     * @return Refund amount (original - platform fee) * tier percentage
     */
    public Money calculateRefund(Money originalAmount, Duration timeUntilSession) {
        // Step 1: Deduct platform fee (non-refundable)
        Money platformFee = originalAmount.multiply(platformFeePercentage);
        Money refundableBase = originalAmount.subtract(platformFee);

        // Step 2: Apply refund tier based on timing
        double refundPercentage = getRefundPercentage(timeUntilSession);

        // Step 3: Calculate final refund
        return refundableBase.multiply(refundPercentage);
    }

    /**
     * Get refund percentage for given time until session.
     */
    private double getRefundPercentage(Duration timeUntilSession) {
        if (timeUntilSession.compareTo(weekBefore.threshold()) >= 0) {
            return weekBefore.percentage();
        } else if (timeUntilSession.compareTo(threeDays.threshold()) >= 0) {
            return threeDays.percentage();
        } else if (timeUntilSession.compareTo(oneDay.threshold()) >= 0) {
            return oneDay.percentage();
        } else if (timeUntilSession.compareTo(lessThanDay.threshold()) >= 0) {
            return lessThanDay.percentage();
        } else {
            return 0.0; // In progress or past
        }
    }

    /**
     * Get refund tier description for given time until session.
     */
    public String getRefundTierDescription(Duration timeUntilSession) {
        double percentage = getRefundPercentage(timeUntilSession);
        if (percentage == 1.0)
            return "100% refund (7+ days before)";
        if (percentage == 0.7)
            return "70% refund (3-7 days before)";
        if (percentage == 0.5)
            return "50% refund (1-3 days before)";
        if (percentage == 0.25)
            return "25% refund (<24 hours before)";
        return "No refund (in progress or past)";
    }

    /**
     * Calculate platform fee for a given amount.
     */
    public Money calculatePlatformFee(Money amount) {
        return amount.multiply(platformFeePercentage);
    }

    /**
     * Calculate mentor payout (amount minus platform fee).
     */
    public Money calculateMentorPayout(Money amount) {
        Money platformFee = calculatePlatformFee(amount);
        return amount.subtract(platformFee);
    }

    /**
     * RefundTier defines a time threshold and refund percentage.
     */
    public record RefundTier(Duration threshold, double percentage) {
        public RefundTier {
            if (percentage < 0.0 || percentage > 1.0) {
                throw new IllegalArgumentException(
                        "Refund percentage must be between 0.0 and 1.0, got: " + percentage);
            }
            if (threshold.isNegative() || threshold.isZero()) {
                throw new IllegalArgumentException(
                        "Refund tier threshold must be positive, got: " + threshold);
            }
        }

        @Override
        public String toString() {
            long days = threshold.toDays();
            long hours = threshold.toHours();
            String timeStr = days > 0 ? days + " day(s)" : hours + " hour(s)";
            return String.format("%s before: %.0f%% refund", timeStr, percentage * 100);
        }
    }
}
