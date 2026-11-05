package com.example.starter.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 150) String name,
        /* Vietnamese mobile/landline: 0xxxxxxxxx (10-11 digits) or +84xxxxxxxxx */
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "{validation.phone.invalid}") String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 255) String address,
        @Size(max = 500) String note
) {
}
