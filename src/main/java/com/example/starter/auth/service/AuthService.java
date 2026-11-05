package com.example.starter.auth.service;

import com.example.starter.auth.dto.AuthResponse;
import com.example.starter.auth.dto.ChangePasswordRequest;
import com.example.starter.auth.dto.ForgotPasswordRequest;
import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.RegisterRequest;
import com.example.starter.auth.dto.ResetPasswordRequest;
import com.example.starter.auth.entity.PasswordResetToken;
import com.example.starter.auth.repository.PasswordResetTokenRepository;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.util.MessageHelper;
import com.example.starter.common.util.TokenGenerator;
import com.example.starter.config.AppProperties;
import com.example.starter.mail.MailService;
import com.example.starter.security.UserPrincipal;
import com.example.starter.security.jwt.JwtService;
import com.example.starter.security.token.TokenBlacklistService;
import com.example.starter.user.entity.Role;
import com.example.starter.user.entity.User;
import com.example.starter.user.mapper.UserMapper;
import com.example.starter.user.repository.RoleRepository;
import com.example.starter.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService blacklistService;
    private final MailService mailService;
    private final MessageHelper messages;
    private final UserMapper userMapper;
    private final AppProperties props;
    private final Clock clock;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, email);
        }
        Role defaultRole = roleRepository.findByCode(Role.USER)
                .orElseThrow(() -> new IllegalStateException("Default role USER missing"));
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setEnabled(true);
        user.getRoles().add(defaultRole);
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findWithRolesByEmailIgnoreCase(principal.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = AppException.class)
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.consume(refreshToken);
        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        return issueTokens(user);
    }

    /** Blacklists the current access token and revokes the refresh token (if provided). */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            try {
                Claims claims = jwtService.parseAccessToken(accessToken);
                blacklistService.revoke(claims.getId(), claims.getExpiration().toInstant());
            } catch (JwtException e) {
                log.debug("Logout with invalid access token: {}", e.getMessage());
            }
        }
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    /** Changes the password and signs the user out from every device. */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }
        updatePassword(user, request.newPassword());
    }

    /**
     * Always succeeds (no user enumeration). When the e-mail exists a one-time link is mailed.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(User::isEnabled)
                .ifPresent(this::sendResetMail);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = clock.instant();
        PasswordResetToken token = resetTokenRepository.findByTokenHash(TokenGenerator.sha256(request.token()))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new AppException(ErrorCode.RESET_TOKEN_INVALID));
        token.setUsedAt(now);
        updatePassword(token.getUser(), request.newPassword());
    }

    @Scheduled(cron = "${app.jobs.purge-tokens-cron:0 0 * * * *}")
    @Transactional
    public void purgeExpiredResetTokens() {
        resetTokenRepository.deleteExpired(clock.instant());
    }

    private void sendResetMail(User user) {
        Instant now = clock.instant();
        resetTokenRepository.invalidateAllForUser(user.getId(), now);
        String raw = TokenGenerator.randomToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(TokenGenerator.sha256(raw));
        token.setUser(user);
        token.setExpiresAt(now.plus(props.auth().passwordResetTtl()));
        resetTokenRepository.save(token);

        String link = UriComponentsBuilder.fromUriString(props.mail().resetPasswordUrl())
                .queryParam("token", raw).build().toUriString();
        long minutes = props.auth().passwordResetTtl().toMinutes();
        mailService.send(MailService.MailMessage.text(user.getEmail(),
                messages.get("mail.reset-password.subject"),
                messages.get("mail.reset-password.body", user.getFullName(), link, minutes)));
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        refreshTokenService.revokeAll(user.getId());
    }

    private AuthResponse issueTokens(User user) {
        JwtService.AccessToken access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getTokenVersion());
        String refresh = refreshTokenService.issue(user);
        return new AuthResponse(access.token(), refresh, "Bearer",
                jwtService.accessTokenTtlSeconds(), userMapper.toResponse(user));
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
