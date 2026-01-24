package com.eduwork.identity.presentation.exception;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.domain.exception.AccountLockedException;
import com.eduwork.identity.domain.exception.EmailAlreadyExistsException;
import com.eduwork.identity.domain.exception.InvalidEmailException;
import com.eduwork.identity.domain.exception.InvalidPasswordException;
import com.eduwork.identity.domain.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * Converts exceptions to standardized ApiResponse format.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        /**
         * Handles validation errors from @Valid annotation.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                List<Map<String, String>> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(this::mapFieldError)
                                .collect(Collectors.toList());

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("VALIDATION_ERROR")
                                .message("Validation failed")
                                .details(errors)
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Validation failed for {}: {} errors", request.getRequestURI(), errors.size());

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handles email already exists error.
         */
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
                        EmailAlreadyExistsException ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("EMAIL_ALREADY_EXISTS")
                                .message(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Email already exists: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        /**
         * Handles invalid password error.
         */
        @ExceptionHandler(InvalidPasswordException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(
                        InvalidPasswordException ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("INVALID_PASSWORD")
                                .message(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Invalid password: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handles invalid email format error.
         */
        @ExceptionHandler(InvalidEmailException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidEmail(
                        InvalidEmailException ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("INVALID_EMAIL")
                                .message(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Invalid email: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(response);
        }

        /**
         * Handles account locked exception.
         */
        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccountLocked(
                        AccountLockedException ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("ACCOUNT_LOCKED")
                                .message(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Account locked: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        /**
         * Handles rate limit exceeded exception.
         */
        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(
                        RateLimitExceededException ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("RATE_LIMIT_EXCEEDED")
                                .message(ex.getMessage())
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.warn("Rate limit exceeded from IP: {}", request.getRemoteAddr());

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }

        /**
         * Handles all other unexpected exceptions.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                ApiResponse.ApiError apiError = ApiResponse.ApiError.builder()
                                .code("INTERNAL_SERVER_ERROR")
                                .message("An unexpected error occurred")
                                .build();

                ApiResponse<Void> response = ApiResponse.error(apiError, request.getRequestURI());

                log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        private Map<String, String> mapFieldError(FieldError fieldError) {
                return Map.of(
                                "field", fieldError.getField(),
                                "message", fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage()
                                                : "Invalid value");
        }
}
