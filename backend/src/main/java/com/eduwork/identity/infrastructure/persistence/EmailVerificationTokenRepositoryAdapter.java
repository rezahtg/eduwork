package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.EmailVerificationToken;
import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.repository.EmailVerificationTokenRepository;
import com.eduwork.identity.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.eduwork.identity.infrastructure.persistence.UserEntity;
import com.eduwork.identity.infrastructure.persistence.mapper.EmailVerificationTokenMapper;
import com.eduwork.identity.infrastructure.persistence.repository.EmailVerificationTokenJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of EmailVerificationTokenRepository.
 */
@Repository
@Transactional
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final EmailVerificationTokenMapper mapper;

    public EmailVerificationTokenRepositoryAdapter(
            EmailVerificationTokenJpaRepository jpaRepository,
            UserJpaRepository userJpaRepository,
            EmailVerificationTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        // Find the user entity
        UserEntity userEntity = userJpaRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + token.getUser().getId()));

        EmailVerificationTokenEntity entity = mapper.toEntity(token, userEntity);
        EmailVerificationTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public List<EmailVerificationToken> findByUserAndCreatedAtAfter(User user, Instant since) {
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
