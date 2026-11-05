package com.example.starter.user;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.security.SecurityUtils;
import com.example.starter.user.dto.ProfileResponse;
import com.example.starter.user.dto.RoleResponse;
import com.example.starter.user.dto.UserCreateRequest;
import com.example.starter.user.dto.UserFilter;
import com.example.starter.user.dto.UserResponse;
import com.example.starter.user.dto.UserStatusRequest;
import com.example.starter.user.dto.UserUpdateRequest;
import com.example.starter.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Current user profile with roles and permissions")
    @GetMapping("/api/v1/users/me")
    public ApiResponse<ProfileResponse> me() {
        return ApiResponse.ok(userService.profile(SecurityUtils.requireUsername()));
    }

    @Operation(summary = "Search users (requires USER_READ)")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/api/v1/users")
    public ApiResponse<PageResponse<UserResponse>> search(
            @ParameterObject UserFilter filter,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(userService.search(filter, pageable));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PostMapping("/api/v1/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/api/v1/users/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @Operation(summary = "Enable / disable an account (disabling signs the user out everywhere)")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PatchMapping("/api/v1/users/{id}/status")
    public ApiResponse<UserResponse> setStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.ok(userService.setEnabled(id, request.enabled()));
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/api/v1/roles")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.ok(userService.roles());
    }
}
