package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ApiException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository users;
    private final RoleRepository roles;

    public AdminUserServiceImpl(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        return users.findAll(pageable);
    }

    @Override
    @Transactional
    public User setEnabled(Long userId, boolean enabled) {
        User u = users.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        u.setEnabled(enabled);
        return users.save(u);
    }

    @Override
    @Transactional
    public User setRoles(Long userId, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw ApiException.badRequest("roles must not be empty");
        }

        User u = users.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        Set<Role> resolved = new HashSet<>();
        for (String raw : roleNames) {
            if (raw == null || raw.isBlank()) {
                throw ApiException.badRequest("Role name must not be blank");
            }
            String name = raw.trim().toUpperCase(Locale.ROOT);

            if (!name.equals("ADMIN") && !name.equals("USER")) {
                throw ApiException.badRequest("Unknown role: " + name);
            }

            Role role = roles.findByName(name)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "BAD_REQUEST",
                            "Role not found in database: " + name
                    ));
            resolved.add(role);
        }

        u.setRoles(resolved);
        return users.save(u);
    }
}
