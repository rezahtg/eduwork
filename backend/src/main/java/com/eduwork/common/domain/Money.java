package com.eduwork.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Money value object representing an amount in a specific currency.
 * Immutable and provides currency-safe arithmetic operations.
 * 
 * Business Rules:
 * - Amount stored in BigDecimal for precision
 * - Currency code validated against ISO 4217
 * - All arithmetic operations return new instances (immutable)
 * - Cannot mix currencies in operations
 */
public final class Money {
    private final BigDecimal amount;
    private final String currencyCode;
    private final Currency currency;

    /**
     * Private constructor to enforce factory methods.
     */
    private Money(BigDecimal amount, String currencyCode) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "Currency code cannot be null").toUpperCase();

        // Validate currency code
        try {
            this.currency = Currency.getInstance(this.currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode, e);
        }

        // Round to currency's default fraction digits
        this.amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }

    // ==================== Factory Methods ====================

    /**
     * Create Money from BigDecimal amount and currency code.
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    /**
     * Create Money from long amount (in smallest unit) and currency code.
     * Example: Money.of(15000000, "IDR") = 150000.00 IDR
     */
    public static Money of(long amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    /**
     * Create Money from double amount and currency code.
     * Note: Prefer BigDecimal or long for precision.
     */
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), currencyCode);
    }

    /**
     * Create zero money in specified currency.
     */
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    // ==================== Getters ====================

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currencyCode;
    }

    public Currency getCurrencyObject() {
        return currency;
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Add two Money values.
     * 
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    /**
     * Subtract another Money value.
     * 
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    /**
     * Multiply by a factor.
     */
    public Money multiply(double factor) {
        BigDecimal result = this.amount.multiply(BigDecimal.valueOf(factor));
        return new Money(result, this.currencyCode);
    }

    /**
     * Multiply by a BigDecimal factor.
     */
    public Money multiply(BigDecimal factor) {
        BigDecimal result = this.amount.multiply(factor);
        return new Money(result, this.currencyCode);
    }

    /**
     * Divide by a factor.
     */
    public Money divide(double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        BigDecimal result = this.amount.divide(BigDecimal.valueOf(divisor), currency.getDefaultFractionDigits(),
                RoundingMode.HALF_UP);
        return new Money(result, this.currencyCode);
    }

    /**
     * Divide by another number (for splitting).
     */
    public Money divide(int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        BigDecimal result = this.amount.divide(BigDecimal.valueOf(divisor), currency.getDefaultFractionDigits(),
                RoundingMode.HALF_UP);
        return new Money(result, this.currencyCode);
    }

    // ==================== Comparison Operations ====================

    /**
     * Check if this money is greater than other.
     */
    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Check if this money is less than other.
     */
    public boolean isLessThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Check if this money is zero.
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Check if this money is positive.
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this money is negative.
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // ==================== Helper Methods ====================

    /**
     * Assert that two Money values have the same currency.
     */
    private void assertSameCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                    String.format("Cannot operate on different currencies: %s and %s",
                            this.currencyCode, other.currencyCode));
        }
    }

    /**
     * Format as string with currency symbol.
     */
    public String format() {
        return String.format("%s %s", currency.getSymbol(), amount.toPlainString());
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Money money))
            return false;
        return amount.compareTo(money.amount) == 0 && currencyCode.equals(money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    @Override
    public String toString() {
        return format();
    }
}
