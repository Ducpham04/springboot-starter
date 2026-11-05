package com.example.starter.auth.dto;

final class PasswordRules {

    /** At least one letter and one digit. Length is checked separately with @Size. */
    static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d).+$";

    private PasswordRules() {
    }
}
