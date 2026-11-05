package com.example.starter.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(@NotNull Boolean enabled) {
}
