package com.example.starter.security.token;

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
public class JpaTokenBlacklistService implements TokenBlacklistService {

    private final RevokedAccessTokenRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public void revoke(String jti, Instant expiresAt) {
        if (!repository.existsById(jti)) {
            repository.save(new RevokedAccessToken(jti, expiresAt));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return jti != null && repository.existsById(jti);
    }

    /** Hourly cleanup of entries whose token has expired anyway. */
    @Scheduled(cron = "${app.jobs.purge-tokens-cron:0 0 * * * *}")
    @Transactional
    public void purgeExpired() {
        int removed = repository.deleteExpired(clock.instant());
        if (removed > 0) {
            log.debug("Purged {} expired revoked access tokens", removed);
        }
    }
}
