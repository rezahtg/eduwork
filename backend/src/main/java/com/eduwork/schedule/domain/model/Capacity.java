package com.eduwork.schedule.domain.model;

/**
 * Capacity defines the minimum and maximum number of students for a session.
 * 
 * Business Rules:
 * - ONE_ON_ONE sessions: min=1, max=1 (enforced)
 * - GROUP sessions: min=1, max=2-100 (configurable)
 * - Max GROUP size limited to 100 to prevent abuse and performance issues
 * 
 * Immutable value object.
 */
public record Capacity(int minStudents, int maxStudents) {
    /**
     * Maximum allowed students in a GROUP session.
     * Limited for:
     * - Quality of teaching
     * - UI/UX scalability
     * - Server performance
     */
    public static final int MAX_GROUP_SIZE = 100;

    /**
     * Compact constructor with validation.
     */
    public Capacity {
        if (minStudents < 1) {
            throw new IllegalArgumentException("Minimum students must be at least 1");
        }
        if (maxStudents < minStudents) {
            throw new IllegalArgumentException(
                    String.format("Maximum students (%d) must be >= minimum students (%d)",
                            maxStudents, minStudents));
        }
        if (maxStudents > MAX_GROUP_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Maximum students (%d) cannot exceed %d",
                            maxStudents, MAX_GROUP_SIZE));
        }
    }

    /**
     * Create capacity for ONE_ON_ONE session.
     * Exactly 1 student.
     */
    public static Capacity oneOnOne() {
        return new Capacity(1, 1);
    }

    /**
     * Create capacity for GROUP session.
     * 
     * @param min Minimum students required to proceed
     * @param max Maximum students allowed
     * @throws IllegalArgumentException if max < 2 or constraints violated
     */
    public static Capacity group(int min, int max) {
        if (max < 2) {
            throw new IllegalArgumentException(
                    "Group sessions must allow at least 2 students (got max=" + max + ")");
        }
        return new Capacity(min, max);
    }

    /**
     * Check if this is a ONE_ON_ONE capacity (exactly 1 student).
     */
    public boolean isOneOnOne() {
        return maxStudents == 1;
    }

    /**
     * Check if this is a GROUP capacity (more than 1 student).
     */
    public boolean isGroup() {
        return maxStudents > 1;
    }

    /**
     * Get available slots based on current enrollment.
     * 
     * @param currentEnrollment Current number of enrolled students
     * @return Number of available slots
     */
    public int getAvailableSlots(int currentEnrollment) {
        return Math.max(0, maxStudents - currentEnrollment);
    }

    /**
     * Check if current enrollment meets minimum requirement.
     * 
     * @param currentEnrollment Current number of enrolled students
     * @return true if minimum is met
     */
    public boolean isMinimumMet(int currentEnrollment) {
        return currentEnrollment >= minStudents;
    }

    /**
     * Check if session is full.
     * 
     * @param currentEnrollment Current number of enrolled students
     * @return true if at maximum capacity
     */
    public boolean isFull(int currentEnrollment) {
        return currentEnrollment >= maxStudents;
    }

    /**
     * Check if can accept one more booking.
     * 
     * @param currentEnrollment Current number of enrolled students
     * @return true if space available
     */
    public boolean canAcceptBooking(int currentEnrollment) {
        return currentEnrollment < maxStudents;
    }

    @Override
    public String toString() {
        if (isOneOnOne()) {
            return "ONE_ON_ONE (1 student)";
        }
        return String.format("GROUP (%d-%d students)", minStudents, maxStudents);
    }
}
