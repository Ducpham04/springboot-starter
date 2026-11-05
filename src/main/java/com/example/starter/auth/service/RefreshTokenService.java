package com.example.starter.auth.service;

import com.example.starter.auth.entity.RefreshToken;
import com.example.starter.auth.repository.RefreshTokenRepository;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.util.TokenGenerator;
import com.example.starter.config.AppProperties;
import com.example.starter.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final AppProperties props;
    private final Clock clock;

    /** Creates a new refresh token and returns the raw value (shown to the client once). */
    @Transactional
    public String issue(User user) {
        String raw = TokenGenerator.randomToken();
        RefreshToken token = new RefreshToken();
        token.setTokenHash(TokenGenerator.sha256(raw));
        token.setUser(user);
        token.setExpiresAt(clock.instant().plus(props.jwt().refreshTokenTtl()));
        repository.save(token);
        return raw;
    }

    /**
     * Validates and revokes the given refresh token (rotation) and returns its owner.
     * Re-using an already revoked token is treated as theft: every token of the user is revoked.
     */
    @Transactional(noRollbackFor = AppException.class)
    public User consume(String rawToken) {
        Instant now = clock.instant();
        RefreshToken token = repository.findByTokenHash(TokenGenerator.sha256(rawToken))
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (token.getRevokedAt() != null) {
            log.warn("Revoked refresh token reused for user {} - revoking all sessions", token.getUser().getId());
            repository.revokeAllForUser(token.getUser().getId(), now);
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (!token.isActive(now)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        token.setRevokedAt(now);
        return token.getUser();
    }

    /** Revokes one token if it exists (logout). Unknown tokens are ignored. */
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(TokenGenerator.sha256(rawToken))
                .filter(t -> t.getRevokedAt() == null)
                .ifPresent(t -> t.setRevokedAt(clock.instant()));
    }

    @Transactional
    public void revokeAll(Long userId) {
        repository.revokeAllForUser(userId, clock.instant());
    }

    @Scheduled(cron = "${app.jobs.purge-tokens-cron:0 0 * * * *}")
    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(clock.instant());
    }
}
