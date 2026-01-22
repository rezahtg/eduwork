package com.eduwork.identity.infrastructure.persistence.mapper;

import com.eduwork.identity.domain.model.PasswordResetToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.infrastructure.persistence.UserEntity;
import com.eduwork.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between PasswordResetToken domain model and
 * PasswordResetTokenEntity.
 */
@Component
public class PasswordResetTokenMapper {

    /**
     * Converts domain model to JPA entity.
     *
     * @param token      the domain model
     * @param userEntity the user entity (from repository)
     * @return JPA entity
     */
    public PasswordResetTokenEntity toEntity(PasswordResetToken token, UserEntity userEntity) {
        return new PasswordResetTokenEntity(
                token.getId(),
                token.getToken(),
                userEntity,
                token.getExpiresAt(),
                token.getCreatedAt(),
                token.getUsedAt());
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity
     * @return domain model
     */
    public PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        UserEntity userEntity = entity.getUser();

        // Map UserEntity to User domain model
        User user = new User(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getPhone(),
                userEntity.getPasswordHash(),
                userEntity.getStatus(),
                userEntity.isProfileComplete(),
                userEntity.getEmailVerifiedAt(),
                userEntity.getPhoneVerifiedAt(),
                null, // lockedUntil not relevant for token mapping
                null, // studentProfile
                null, // mentorProfile
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt());

        return new PasswordResetToken(
                entity.getId(),
                entity.getToken(),
                user,
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedAt());
    }
}
