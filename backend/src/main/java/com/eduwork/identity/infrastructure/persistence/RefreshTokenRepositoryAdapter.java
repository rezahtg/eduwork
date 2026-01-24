package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.RefreshToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.RefreshTokenRepository;
import com.eduwork.identity.infrastructure.persistence.entity.RefreshTokenEntity;
import com.eduwork.identity.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.eduwork.identity.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * JPA implementation of RefreshTokenRepository.
 */
@Repository
@Transactional
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryAdapter(
            RefreshTokenJpaRepository jpaRepository,
            UserJpaRepository userJpaRepository,
            RefreshTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        // Find the user entity
        UserEntity userEntity = userJpaRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + token.getUser().getId()));

        RefreshTokenEntity entity = mapper.toEntity(token, userEntity);
        RefreshTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String tokenId) {
        return jpaRepository.findByToken(tokenId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findActiveTokenByUser(User user) {
        UserEntity userEntity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));

        return jpaRepository.findActiveTokenByUser(userEntity, Instant.now())
                .map(mapper::toDomain);
    }

    @Override
    public int deleteExpiredTokens(Instant now) {
        return jpaRepository.deleteExpiredTokens(now);
    }

    @Override
    public void revokeAllByUser(User user) {
        UserEntity userEntity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));

        jpaRepository.revokeAllByUser(userEntity, Instant.now());
    }
}
