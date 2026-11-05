package com.example.starter.security.token;

import java.time.Instant;

/**
 * Access-token blacklist used by logout. The default implementation is DB backed
 * ({@link JpaTokenBlacklistService}); provide another bean (e.g. Redis with TTL) and mark it
 * {@code @Primary} to replace it.
 */
public interface TokenBlacklistService {

    void revoke(String jti, Instant expiresAt);

    boolean isRevoked(String jti);
}
