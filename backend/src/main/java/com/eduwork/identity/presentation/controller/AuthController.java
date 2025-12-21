package com.eduwork.identity.presentation.controller;

import com.eduwork.identity.application.command.RegisterUserCommand;
import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.application.usecase.RegisterUserUseCase;
import com.eduwork.identity.presentation.dto.RegisterRequest;
import com.eduwork.identity.presentation.dto.UserResponse;
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
