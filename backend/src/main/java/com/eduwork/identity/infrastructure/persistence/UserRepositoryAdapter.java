package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
// Caching temporarily disabled due to domain model serialization issues
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements domain UserRepository using JPA.
 * Translates between domain User and infrastructure UserEntity.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    // @Caching(evict = {
    // @CacheEvict(value = "users", key = "#user.id"),
    // @CacheEvict(value = "users", key = "'email:' + #user.email.toLowerCase()")
    // })
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    // @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    // @Cacheable(value = "users", key = "'email:' + #email.toLowerCase()")
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmailIgnoreCase(email)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return jpaRepository.existsByPhone(phone);
    }

    /**
     * Converts domain User to JPA UserEntity.
     */
    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getPasswordHash(),
                user.getStatus(),
                user.isProfileComplete(),
                user.getEmailVerifiedAt(),
                user.getPhoneVerifiedAt(),
                user.getLockedUntil(),
                user.getStudentProfile(),
                user.getMentorProfile(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /**
     * Converts JPA UserEntity to domain User.
     */
    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getStatus(),
                entity.isProfileComplete(),
                entity.getEmailVerifiedAt(),
                entity.getPhoneVerifiedAt(),
                entity.getLockedUntil(),
                entity.getStudentProfile(),
                entity.getMentorProfile(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
