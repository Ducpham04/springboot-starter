package com.example.starter.auth;

import com.example.starter.auth.dto.AuthResponse;
import com.example.starter.auth.dto.ChangePasswordRequest;
import com.example.starter.auth.dto.ForgotPasswordRequest;
import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.LogoutRequest;
import com.example.starter.auth.dto.RefreshTokenRequest;
import com.example.starter.auth.dto.RegisterRequest;
import com.example.starter.auth.dto.ResetPasswordRequest;
import com.example.starter.auth.service.AuthService;
import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.util.MessageHelper;
import com.example.starter.security.SecurityUtils;
import com.example.starter.security.jwt.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageHelper messages;

    @Operation(summary = "Register a new account (role USER)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @Operation(summary = "Login with e-mail and password")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "Exchange a refresh token for a new token pair (rotation)")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Logout: blacklist the access token and revoke the refresh token")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest,
                                    @RequestBody(required = false) LogoutRequest request) {
        authService.logout(JwtAuthenticationFilter.resolveToken(httpRequest),
                request == null ? null : request.refreshToken());
        return ApiResponse.ok(null, messages.get("auth.logout.success"));
    }

    @Operation(summary = "Change password of the current user (signs out all sessions)")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.requireUser().getId(), request);
        return ApiResponse.ok(null, messages.get("auth.password.changed"));
    }

    @Operation(summary = "Send a reset-password link by e-mail")
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null, messages.get("auth.forgot-password.sent"));
    }

    @Operation(summary = "Reset password with the token received by e-mail")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null, messages.get("auth.password.reset"));
    }
}
