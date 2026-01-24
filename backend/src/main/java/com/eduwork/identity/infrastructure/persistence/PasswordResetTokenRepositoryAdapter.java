package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.PasswordResetToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.PasswordResetTokenRepository;
import com.eduwork.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.eduwork.identity.infrastructure.persistence.mapper.PasswordResetTokenMapper;
import com.eduwork.identity.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of PasswordResetTokenRepository.
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordResetTokenMapper mapper;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        // Find the user entity
        UserEntity userEntity = userJpaRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + token.getUser().getId()));

        PasswordResetTokenEntity entity = mapper.toEntity(token, userEntity);
        PasswordResetTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public List<PasswordResetToken> findByUserAndCreatedAtAfter(User user, Instant since) {
        UserEntity userEntity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));

        return jpaRepository.findByUserAndCreatedAtAfter(userEntity, since)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteExpiredTokens(Instant now) {
        return jpaRepository.deleteExpiredTokens(now);
    }

    @Override
    public void deleteByUser(User user) {
        UserEntity userEntity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));

        jpaRepository.deleteByUser(userEntity);
    }
}
