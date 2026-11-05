package com.example.starter.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100)
        @Pattern(regexp = PasswordRules.REGEX, message = "{validation.password.weak}") String password,
        @NotBlank @Size(max = 150) String fullName,
        @Pattern(regexp = "^[0-9+]{8,20}$", message = "{validation.phone.invalid}") String phone
) {
}
