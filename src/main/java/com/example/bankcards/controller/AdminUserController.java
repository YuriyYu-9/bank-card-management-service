package com.example.bankcards.controller;

import com.example.bankcards.dto.UserEnabledUpdateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.UserRolesUpdateRequest;
import com.example.bankcards.service.AdminUserService;
import com.example.bankcards.util.UserMapper;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUsers;

    public AdminUserController(AdminUserService adminUsers) {
        this.adminUsers = adminUsers;
    }

    @GetMapping
    public Page<UserResponse> list(@ParameterObject Pageable pageable) {
        return adminUsers.listUsers(pageable).map(UserMapper::toResponse);
    }

    @PatchMapping("/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id, @Valid @RequestBody UserEnabledUpdateRequest req) {
        return UserMapper.toResponse(adminUsers.setEnabled(id, req.enabled()));
    }

    @PutMapping("/{id}/roles")
    public UserResponse setRoles(@PathVariable Long id, @Valid @RequestBody UserRolesUpdateRequest req) {
        return UserMapper.toResponse(adminUsers.setRoles(id, req.roles()));
    }
}
