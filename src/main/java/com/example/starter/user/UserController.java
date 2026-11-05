package com.example.starter.user;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.security.SecurityUtils;
import com.example.starter.user.dto.UserFilter;
import com.example.starter.user.dto.UserResponse;
import com.example.starter.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Current user profile")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getByEmail(SecurityUtils.requireUsername()));
    }

    @Operation(summary = "Search users (requires USER_READ)")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> search(
            @ParameterObject UserFilter filter,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(userService.search(filter, pageable));
    }
}
