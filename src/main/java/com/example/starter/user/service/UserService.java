package com.example.starter.user.service;

import com.example.starter.auth.service.RefreshTokenService;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.security.SecurityUtils;
import com.example.starter.user.dto.ProfileResponse;
import com.example.starter.user.dto.RoleResponse;
import com.example.starter.user.dto.UserCreateRequest;
import com.example.starter.user.dto.UserFilter;
import com.example.starter.user.dto.UserResponse;
import com.example.starter.user.dto.UserUpdateRequest;
import com.example.starter.user.entity.Role;
import com.example.starter.user.entity.User;
import com.example.starter.user.mapper.UserMapper;
import com.example.starter.user.repository.RoleRepository;
import com.example.starter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserResponse getByEmail(String email) {
        return userRepository.findWithRolesByEmailIgnoreCase(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, email));
    }

    public ProfileResponse profile(String email) {
        User user = userRepository.findWithRolesByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, email));
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toCollection(java.util.TreeSet::new));
        Set<String> roles = user.getRoles().stream().map(Role::getCode)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
        return new ProfileResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(), roles, permissions);
    }

    public PageResponse<UserResponse> search(UserFilter filter, Pageable pageable) {
        Specification<User> spec = SpecificationBuilder.<User>create()
                .likeAny(filter.keyword(), "email", "fullName", "phone")
                .equal("enabled", filter.enabled())
                .equal("roles.code", filter.role())
                .build();
        return PageResponse.of(userRepository.findAll(spec, pageable), userMapper::toResponse);
    }

    public List<RoleResponse> roles() {
        return roleRepository.findAll(Sort.by("id")).stream()
                .map(r -> new RoleResponse(r.getCode(), r.getName()))
                .toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, email);
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhone(blankToNull(request.phone()));
        user.setEnabled(true);
        user.setRoles(resolveRoles(request.roles()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findOrThrow(id);
        Set<Role> roles = resolveRoles(request.roles());
        if (isSelf(user) && roles.stream().noneMatch(r -> Role.ADMIN.equals(r.getCode()))) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SELF);
        }
        user.setFullName(request.fullName().trim());
        user.setPhone(blankToNull(request.phone()));
        user.setRoles(roles);
        return userMapper.toResponse(userRepository.save(user));
    }

    /** Disabling also revokes every session of the user immediately. */
    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findOrThrow(id);
        if (isSelf(user) && !enabled) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SELF);
        }
        user.setEnabled(enabled);
        if (!enabled) {
            user.setTokenVersion(user.getTokenVersion() + 1);
            refreshTokenService.revokeAll(user.getId());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<String> codes) {
        Set<Role> roles = new HashSet<>();
        for (String code : codes) {
            roles.add(roleRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, code)));
        }
        return roles;
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, id));
    }

    private static boolean isSelf(User user) {
        return SecurityUtils.currentUser().map(p -> p.getId().equals(user.getId())).orElse(false);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
