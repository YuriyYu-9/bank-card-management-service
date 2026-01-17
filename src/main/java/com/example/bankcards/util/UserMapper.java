package com.example.bankcards.util;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User u) {
        var roles = u.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toUnmodifiableSet());

        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.isEnabled(),
                roles,
                u.getCreatedAt()
        );
    }
}
