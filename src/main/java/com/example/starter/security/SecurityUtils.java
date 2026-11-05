package com.example.starter.security;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return auth.getPrincipal() instanceof UserPrincipal p ? Optional.of(p) : Optional.empty();
    }

    public static Optional<String> currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(auth.getName());
    }

    public static UserPrincipal requireUser() {
        return currentUser().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    public static String requireUsername() {
        return currentUsername().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }
}
