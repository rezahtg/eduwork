package com.eduwork.schedule.domain.model;

/**
 * SessionType defines whether a session is one-on-one or group.
 * 
 * Business Rules:
 * - ONE_ON_ONE: Exactly 1 student, premium pricing
 * - GROUP: 2-100 students, lower per-student pricing
 */
public enum SessionType {
    /**
     * Private one-on-one tutoring session.
     * - Exactly 1 student
     * - Full attention from mentor
     * - Premium pricing
     */
    ONE_ON_ONE {
        @Override
        public Capacity defaultCapacity() {
            return Capacity.oneOnOne();
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public String getDescription() {
            return "One-on-one private tutoring";
        }
    },

    /**
     * Group classroom-style session.
     * - 2-100 students
     * - Collaborative learning
     * - Lower per-student pricing
     */
    GROUP {
        @Override
        public Capacity defaultCapacity() {
            return Capacity.group(5, 20);
        }

        @Override
        public boolean isGroup() {
            return true;
        }

        @Override
        public String getDescription() {
            return "Group learning session";
        }
    };

    /**
     * Get the default capacity for this session type.
     */
    public abstract Capacity defaultCapacity();

    /**
     * Check if this is a group session.
     */
    public abstract boolean isGroup();

    /**
     * Get human-readable description.
     */
    public abstract String getDescription();
}
