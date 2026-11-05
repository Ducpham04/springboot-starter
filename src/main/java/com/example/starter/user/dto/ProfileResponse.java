package com.example.starter.user.dto;

import java.util.Set;

/** Current user + effective permissions, used by the front end to show/hide features. */
public record ProfileResponse(Long id, String email, String fullName, String phone,
                              Set<String> roles, Set<String> permissions) {
}
