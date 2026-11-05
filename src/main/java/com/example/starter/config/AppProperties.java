package com.example.starter.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/** All custom settings live under the {@code app.*} prefix (see application.yml). */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cors cors,
        @Valid @NotNull Storage storage,
        @Valid @NotNull Mail mail,
        @Valid @NotNull Auth auth,
        @NotNull Locale defaultLocale,
        @Valid Admin admin,
        /* Install the Vietnamese demo data set (Flyway location db/demo). */
        boolean demoData
) {

    public record Jwt(
            /* Base64-encoded HMAC key, at least 256 bits (32 bytes). */
            @NotBlank String secret,
            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl
    ) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Storage(
            /* Implementation of StorageService to use: local (default) or a custom one such as s3. */
            @NotBlank String provider,
            @NotBlank String localDir,
            @NotNull DataSize maxFileSize,
            List<String> allowedContentTypes
    ) {
    }

    public record Mail(
            /* log = print mails to the log (dev), smtp = send via spring.mail.* */
            @NotBlank String provider,
            @NotBlank String from,
            /* Front-end page that receives ?token=... to reset the password. */
            @NotBlank String resetPasswordUrl
    ) {
    }

    public record Auth(
            @NotNull Duration passwordResetTtl,
            /* Public self sign-up (POST /auth/register). Off for internal shop software. */
            boolean registrationEnabled
    ) {
    }

    /** Optional bootstrap admin, created at startup when no user with this email exists. */
    public record Admin(String email, String password, String fullName) {
    }
}
