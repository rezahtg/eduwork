package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepositoryAdapter.
 * Uses in-memory H2 database for testing.
 * Flyway is disabled for these tests.
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("UserRepositoryAdapter Integration Tests")
class UserRepositoryAdapterTest {

    @Autowired
    private UserJpaRepository jpaRepository;

    private UserRepositoryAdapter repositoryAdapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        repositoryAdapter = new UserRepositoryAdapter(jpaRepository);
    }

    @Test
    @DisplayName("Should save and retrieve user")
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = new User("test@example.com", "$2a$12$hash", "+6281234567890");

        // When
        User saved = repositoryAdapter.save(user);
        Optional<User> retrieved = repositoryAdapter.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals("test@example.com", retrieved.get().getEmail());
        assertEquals("+6281234567890", retrieved.get().getPhone());
        assertEquals(UserStatus.PENDING_VERIFICATION, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should find user by email case-insensitively")
    void shouldFindByEmail_caseInsensitive() {
        // Given
        User user = new User("Test@Example.COM", "$2a$12$hash", null);
        repositoryAdapter.save(user);

        // When
        Optional<User> found = repositoryAdapter.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should check email existence case-insensitively")
    void shouldCheckEmailExists_caseInsensitive() {
        // Given
        User user = new User("Test@Example.COM", "$2a$12$hash", null);
        repositoryAdapter.save(user);

        // When & Then
        assertTrue(repositoryAdapter.existsByEmail("test@example.com"));
        assertTrue(repositoryAdapter.existsByEmail("TEST@EXAMPLE.COM"));
        assertFalse(repositoryAdapter.existsByEmail("other@example.com"));
    }

    @Test
    @DisplayName("Should check phone existence")
    void shouldCheckPhoneExists() {
        // Given
        User user = new User("test@example.com", "$2a$12$hash", "+6281234567890");
        repositoryAdapter.save(user);

        // When & Then
        assertTrue(repositoryAdapter.existsByPhone("+6281234567890"));
        assertFalse(repositoryAdapter.existsByPhone("+6289999999999"));
    }

    @Test
    @DisplayName("Should return false when checking null phone")
    void shouldReturnFalse_whenCheckingNullPhone() {
        // When & Then
        assertFalse(repositoryAdapter.existsByPhone(null));
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
        // Given
        User user = new User("test@example.com", "$2a$12$hash", null);
        User saved = repositoryAdapter.save(user);

        // When
        saved.verifyEmail();
        User updated = repositoryAdapter.save(saved);

        // Then
        assertEquals(UserStatus.ACTIVE, updated.getStatus());
        assertNotNull(updated.getEmailVerifiedAt());
    }
}
