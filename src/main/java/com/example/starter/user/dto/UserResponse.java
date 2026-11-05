package com.example.starter.user.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        boolean enabled,
        Set<String> roles,
        Instant createdAt
) {
}
