package com.example.starter.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 100)
        @Pattern(regexp = PasswordRules.REGEX, message = "{validation.password.weak}") String newPassword
) {
}
