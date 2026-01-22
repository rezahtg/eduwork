package com.eduwork.identity.infrastructure.persistence.mapper;

import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.infrastructure.persistence.UserEntity;
import com.eduwork.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between RefreshToken domain model and
 * RefreshTokenEntity.
 */
@Component
public class RefreshTokenMapper {

    /**
     * Converts domain model to JPA entity.
     *
     * @param token      the domain model
     * @param userEntity the user entity (from repository)
     * @return JPA entity
     */
    public RefreshTokenEntity toEntity(RefreshToken token, UserEntity userEntity) {
        return new RefreshTokenEntity(
                token.getId(),
                token.getToken(),
                userEntity,
                token.getExpiresAt(),
                token.getCreatedAt(),
                token.getRevokedAt());
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity
     * @return domain model
     */
    public RefreshToken toDomain(RefreshTokenEntity entity) {
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
                null, // studentProfile - not needed in token mapper
                null, // mentorProfile - not needed in token mapper
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt());

        return new RefreshToken(
                entity.getId(),
                entity.getToken(),
                user,
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt());
    }
}
