package com.eduwork.identity.presentation.controller;

import com.eduwork.identity.application.command.LoginCommand;
import com.eduwork.identity.application.command.RegisterUserCommand;
import com.eduwork.identity.application.command.RequestPasswordResetCommand;
import com.eduwork.identity.application.command.ResetPasswordCommand;
import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.dto.AuthenticationResponse;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.application.usecase.*;
import com.eduwork.identity.infrastructure.util.RequestUtils;
import com.eduwork.identity.presentation.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and email verification.
 * All responses wrapped in standardized ApiResponse format.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

        private final RegisterUserUseCase registerUserUseCase;
        private final VerifyEmailUseCase verifyEmailUseCase;
        private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;
        private final LoginUseCase loginUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;
        private final LogoutUseCase logoutUseCase;
        private final RequestPasswordResetUseCase requestPasswordResetUseCase;
        private final ResetPasswordUseCase resetPasswordUseCase;

        /**
         * Register a new user (student or mentor).
         * 
         * @param request registration request with email, password, role
         * @return created user response
         *         Response format:
         *         {
         *         "success": true,
         *         "timestamp": "2025-12-21T08:00:00Z",
         *         "data": { user details },
         *         "message": "User registered successfully. Please check your email to
         *         verify your account."
         *         }
         */
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
                log.info("Registration request received for email: {}", request.getEmail());

                // Map REST DTO to application command
                RegisterUserCommand command = RegisterUserCommand.builder()
                                .email(request.getEmail())
                                .password(request.getPassword())
                                .phone(request.getPhone())
                                .role(request.getRole())
                                .build();

                // Execute use case
                UserResponseDTO userDTO = registerUserUseCase.execute(command);

                // Map application DTO to REST response
                UserResponse response = mapToResponse(userDTO);

                log.info("User registered successfully: userId={}", response.getId());

                // Wrap in standardized ApiResponse
                ApiResponse<UserResponse> apiResponse = ApiResponse.success(
                                response,
                                "User registered successfully. Please check your email to verify your account.");

                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }

        /**
         * Verify user email address using token.
         * 
         * @param token verification token from email link
         * @return success response
         *         Response format:
         *         {
         *         "success": true,
         *         "timestamp": "2025-12-21T08:00:00Z",
         *         "message": "Email verified successfully! You can now log in."
         *         }
         */
        @GetMapping("/verify-email")
        public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
                log.info("Email verification request received");

                verifyEmailUseCase.execute(token);

                ApiResponse<Void> apiResponse = ApiResponse.success(
                                null,
                                "Email verified successfully! You can now log in.");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Resend verification email to user.
         * 
         * @param request email address to resend verification
         * @return success response
         *         Response format:
         *         {
         *         "success": true,
         *         "timestamp": "2025-12-21T08:00:00Z",
         *         "message": "Verification email sent. Please check your inbox."
         *         }
         */
        @PostMapping("/resend-verification")
        public ResponseEntity<ApiResponse<Void>> resendVerification(
                        @Valid @RequestBody ResendVerificationRequest request) {
                log.info("Resend verification request received for email: {}", request.getEmail());

                resendVerificationEmailUseCase.execute(request.getEmail());

                ApiResponse<Void> apiResponse = ApiResponse.success(
                                null,
                                "Verification email sent. Please check your inbox.");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Login endpoint.
         *
         * @param request     login credentials
         * @param httpRequest HTTP request for IP extraction
         * @return authentication response with tokens
         */
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {

                log.info("Login request received for email: {}", request.getEmail());

                // Extract client IP address
                String ipAddress = RequestUtils.getClientIpAddress(httpRequest);

                // Extract User-Agent for session tracking
                String deviceInfo = httpRequest.getHeader("User-Agent");

                // Map REST DTO to application command
                LoginCommand command = LoginCommand.builder()
                                .email(request.getEmail())
                                .password(request.getPassword())
                                .deviceInfo(deviceInfo)
                                .build();

                // Execute use case with IP address
                AuthenticationResponse response = loginUseCase.execute(command, ipAddress);

                log.info("Login successful");

                // Wrap in standardized ApiResponse
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.success(
                                response,
                                "Login successful");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Refresh access token using refresh token.
         * 
         * @param request refresh token
         * @return new tokens
         */
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
                        @Valid @RequestBody RefreshTokenRequest request) {
                log.info("Token refresh request received");

                // Execute use case
                AuthenticationResponse response = refreshTokenUseCase.execute(request.getRefreshToken());

                log.info("Token refresh successful");

                // Wrap in standardized ApiResponse
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.success(
                                response,
                                "Token refreshed successfully");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Logout user by revoking refresh token.
         * 
         * @param request refresh token to revoke
         * @return success response
         */
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
                log.info("Logout request received");

                // Execute use case
                logoutUseCase.execute(request.getRefreshToken());

                log.info("Logout successful");

                // Wrap in standardized ApiResponse
                ApiResponse<Void> apiResponse = ApiResponse.success(
                                null,
                                "Logged out successfully");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Request password reset (Step 1: Send reset email).
         * Security: Always returns success to prevent email enumeration.
         * 
         * @param request email address
         * @return success message
         */
        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                log.info("Password reset requested for email: {}", request.getEmail());

                // Map to command
                RequestPasswordResetCommand command = RequestPasswordResetCommand.builder()
                                .email(request.getEmail())
                                .build();

                // Execute use case
                requestPasswordResetUseCase.execute(command);

                // Always return success (security: prevent email enumeration)
                ApiResponse<Void> apiResponse = ApiResponse.success(
                                null,
                                "If your email exists, you will receive password reset instructions.");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Reset password using token (Step 2: Submit new password).
         * 
         * @param request token and new password
         * @return success message
         */
        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                log.info("Password reset attempt with token");

                // Map to command
                ResetPasswordCommand command = ResetPasswordCommand.builder()
                                .token(request.getToken())
                                .newPassword(request.getNewPassword())
                                .build();

                // Execute use case
                resetPasswordUseCase.execute(command);

                log.info("Password reset successful");

                // Return success
                ApiResponse<Void> apiResponse = ApiResponse.success(
                                null,
                                "Password reset successfully. You can now login with your new password.");

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * Maps application DTO to REST response.
         */
        private UserResponse mapToResponse(UserResponseDTO dto) {
                return UserResponse.builder()
                                .id(dto.getId())
                                .email(dto.getEmail())
                                .phone(dto.getPhone())
                                .status(dto.getStatus())
                                .profileComplete(dto.isProfileComplete())
                                .createdAt(dto.getCreatedAt())
                                .build();
        }
}
