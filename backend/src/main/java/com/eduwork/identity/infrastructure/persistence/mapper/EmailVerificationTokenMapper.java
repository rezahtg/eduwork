package com.eduwork.identity.infrastructure.persistence.mapper;

import com.eduwork.identity.domain.model.EmailVerificationToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.eduwork.identity.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for EmailVerificationToken domain and persistence entities.
 */
@Component
public class EmailVerificationTokenMapper {

    /**
     * Convert domain model to JPA entity.
     *
     * @param token      domain model
     * @param userEntity the user JPA entity
     * @return JPA entity
     */
    public EmailVerificationTokenEntity toEntity(EmailVerificationToken token, UserEntity userEntity) {
        if (token == null) {
            return null;
        }

        return new EmailVerificationTokenEntity(
                token.getId(),
                token.getToken(),
                userEntity,
                token.getExpiresAt(),
                token.getCreatedAt(),
                token.getUsedAt());
    }

    /**
     * Convert JPA entity to domain model.
     *
     * @param entity JPA entity
     * @return domain model
     */
    public EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        // Convert UserEntity to User domain model
        UserEntity userEntity = entity.getUser();
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

        return new EmailVerificationToken(
                entity.getId(),
                entity.getToken(),
                user,
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedAt());
    }
}
