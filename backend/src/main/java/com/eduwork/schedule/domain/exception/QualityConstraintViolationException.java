package com.eduwork.schedule.domain.exception;

import java.util.List;

/**
 * Exception thrown when quality constraints are violated.
 * Contains a list of specific violations for detailed error reporting.
 */
public class QualityConstraintViolationException extends ScheduleValidationException {

    private final List<String> violations;

    public QualityConstraintViolationException(List<String> violations) {
        super(buildMessage(violations));
        this.violations = violations;
    }

    public List<String> getViolations() {
        return violations;
    }

    private static String buildMessage(List<String> violations) {
        if (violations.isEmpty()) {
            return "Quality constraint violation";
        }
        return "Quality constraints violated: " + String.join(", ", violations);
    }
}
