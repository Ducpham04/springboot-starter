package com.example.starter.auth.dto;

/** The refresh token is optional: when given it is revoked together with the access token. */
public record LogoutRequest(String refreshToken) {
}
