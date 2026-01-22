package com.eduwork.identity.presentation.controller;

import com.eduwork.identity.domain.model.UserRole;
import com.eduwork.identity.presentation.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests full stack: Controller → Use Case → Repository → H2 Database
 * Uses H2 in-memory database with JPA auto-DDL (Flyway disabled).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Should register user successfully with valid request")
        void shouldRegisterUser_whenValidRequest() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("newuser@example.com")
                                .password("SecurePass123!")
                                .phone("+6281234567890")
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then - Verify standardized ApiResponse structure
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.message").value(containsString("registered successfully")))
                                .andExpect(jsonPath("$.data.id").exists())
                                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                                .andExpect(jsonPath("$.data.phone").value("+6281234567890"))
                                .andExpect(jsonPath("$.data.status").value("PENDING_VERIFICATION"))
                                .andExpect(jsonPath("$.data.profileComplete").value(false))
                                .andExpect(jsonPath("$.data.createdAt").exists());
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn409_whenEmailExists() throws Exception {
                // Given - register first user
                RegisterRequest firstRequest = RegisterRequest.builder()
                                .email("duplicate@example.com")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)));

                // When - try to register with same email
                RegisterRequest duplicateRequest = RegisterRequest.builder()
                                .email("duplicate@example.com")
                                .password("DifferentPass123!")
                                .role(UserRole.MENTOR)
                                .build();

                // Then - Verify standardized error response
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"))
                                .andExpect(jsonPath("$.error.message")
                                                .value(containsString("Email already registered")))
                                .andExpect(jsonPath("$.path").value("/auth/register"));
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400_whenEmailInvalid() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("invalid-email")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.error.details[0].field").value("email"))
                                .andExpect(jsonPath("$.error.details[0].message").value("Email must be valid"));
        }

        @Test
        @DisplayName("Should return 400 when password is weak")
        void shouldReturn400_whenPasswordWeak() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("test@example.com")
                                .password("weak")
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.error.details[0].field").value("password"));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400_whenRequiredFieldsMissing() throws Exception {
                // Given - missing email and password
                RegisterRequest request = RegisterRequest.builder()
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.error.details", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Should register user without phone when phone is null")
        void shouldRegisterUser_whenPhoneIsNull() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("nophone@example.com")
                                .password("SecurePass123!")
                                .phone(null)
                                .role(UserRole.MENTOR)
                                .build();

                // When & Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.phone").doesNotExist());
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmail_toLowerCase() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("Test@EXAMPLE.COM")
                                .password("SecurePass123!")
                                .role(UserRole.STUDENT)
                                .build();

                // When & Then
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }
}

