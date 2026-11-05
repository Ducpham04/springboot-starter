package com.example.starter.security;

import com.example.starter.config.AppProperties;
import com.example.starter.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWludGVncmF0aW9uLXRlc3RzLTMyYg==";
    private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTMyLWJ5dGVzLW1pbg==";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private static JwtService service(String secret, Instant now) {
        AppProperties props = new AppProperties(
                new AppProperties.Jwt(secret, "test", Duration.ofMinutes(15), Duration.ofDays(7)),
                null, null, null, null, Locale.ENGLISH, null, false);
        return new JwtService(props, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void generatesAndParsesAccessToken() {
        JwtService jwt = service(SECRET, NOW);
        JwtService.AccessToken token = jwt.generateAccessToken(42L, "a@b.c", 3);

        Claims claims = jwt.parseAccessToken(token.token());

        assertThat(claims.getSubject()).isEqualTo("a@b.c");
        assertThat(claims.getId()).isEqualTo(token.jti());
        assertThat(claims.get(JwtService.CLAIM_UID, Long.class)).isEqualTo(42L);
        assertThat(claims.get(JwtService.CLAIM_VERSION, Integer.class)).isEqualTo(3);
        assertThat(token.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void rejectsExpiredToken() {
        String token = service(SECRET, NOW).generateAccessToken(1L, "a@b.c", 0).token();
        JwtService later = service(SECRET, NOW.plus(Duration.ofMinutes(16)));

        assertThatThrownBy(() -> later.parseAccessToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = service(OTHER_SECRET, NOW).generateAccessToken(1L, "a@b.c", 0).token();

        assertThatThrownBy(() -> service(SECRET, NOW).parseAccessToken(token)).isInstanceOf(JwtException.class);
    }
}
