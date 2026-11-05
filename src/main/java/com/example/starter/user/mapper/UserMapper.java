package com.example.starter.user.mapper;

import com.example.starter.user.dto.UserResponse;
import com.example.starter.user.entity.Role;
import com.example.starter.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(roleCodes(user.getRoles()))")
    UserResponse toResponse(User user);

    default Set<String> roleCodes(Set<Role> roles) {
        return roles.stream().map(Role::getCode).collect(Collectors.toSet());
    }
}
