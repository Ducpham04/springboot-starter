package com.example.starter.user.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.user.dto.UserFilter;
import com.example.starter.user.dto.UserResponse;
import com.example.starter.user.entity.User;
import com.example.starter.user.mapper.UserMapper;
import com.example.starter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getByEmail(String email) {
        return userRepository.findWithRolesByEmailIgnoreCase(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, email));
    }

    public PageResponse<UserResponse> search(UserFilter filter, Pageable pageable) {
        Specification<User> spec = SpecificationBuilder.<User>create()
                .likeAny(filter.keyword(), "email", "fullName", "phone")
                .equal("enabled", filter.enabled())
                .equal("roles.code", filter.role())
                .build();
        return PageResponse.of(userRepository.findAll(spec, pageable), userMapper::toResponse);
    }
}
