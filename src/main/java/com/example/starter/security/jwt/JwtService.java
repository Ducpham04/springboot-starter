package com.example.starter.security.jwt;

import com.example.starter.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and verifies short-lived access tokens (HS256).
 * Refresh tokens are opaque random strings stored hashed in the DB, see RefreshTokenService.
 */
@Service
public class JwtService {

    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_VERSION = "ver";
    public static final String TYPE_ACCESS = "access";

    private final SecretKey key;
    private final AppProperties.Jwt props;
    private final Clock clock;

    public JwtService(AppProperties appProperties, Clock clock) {
        this.props = appProperties.jwt();
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        this.clock = clock;
    }

    public AccessToken generateAccessToken(Long userId, String email, int tokenVersion) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(props.accessTokenTtl());
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(jti)
                .issuer(props.issuer())
                .subject(email)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_VERSION, tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new AccessToken(token, jti, expiresAt);
    }

    /**
     * @throws io.jsonwebtoken.ExpiredJwtException if expired
     * @throws JwtException for any other invalid token
     */
    public Claims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Not an access token");
        }
        return claims;
    }

    public long accessTokenTtlSeconds() {
        return props.accessTokenTtl().toSeconds();
    }

    public record AccessToken(String token, String jti, Instant expiresAt) {
    }
}
