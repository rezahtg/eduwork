package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port (interface) for User aggregate.
 * Implementation will be in infrastructure layer.
 */
public interface UserRepository {

    /**
     * Saves a user (create or update).
     * 
     * @param user user to save
     * @return saved user
     */
    User save(User user);

    /**
     * Finds user by ID.
     * 
     * @param id user ID
     * @return user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Finds user by email (case-insensitive).
     * 
     * @param email user email
     * @return user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if email already exists.
     * 
     * @param email email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if phone already exists.
     * 
     * @param phone phone to check
     * @return true if exists
     */
    boolean existsByPhone(String phone);
}
