package com.example.starter.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Pattern(regexp = "^[0-9+]{8,20}$", message = "{validation.phone.invalid}") String phone,
        @NotEmpty Set<@NotBlank String> roles
) {
}
