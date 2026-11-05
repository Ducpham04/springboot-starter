package com.example.starter.user.dto;

/** Query parameters for GET /api/v1/users. All fields are optional. */
public record UserFilter(String keyword, Boolean enabled, String role) {
}
