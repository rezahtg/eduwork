package com.eduwork.schedule.presentation.exception;

import com.eduwork.schedule.domain.exception.QualityConstraintViolationException;
import com.eduwork.schedule.domain.exception.ScheduleValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for schedule-related exceptions.
 */
@Slf4j
@RestControllerAdvice
public class ScheduleExceptionHandler {

    @ExceptionHandler(ScheduleValidationException.class)
    public ResponseEntity<Map<String, Object>> handleScheduleValidationException(
            ScheduleValidationException ex) {
        log.warn("Schedule validation error: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(QualityConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleQualityConstraintViolationException(
            QualityConstraintViolationException ex) {
        log.warn("Quality constraint violation: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Quality Constraint Violation");
        body.put("message", ex.getMessage());
        body.put("violations", ex.getViolations());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
