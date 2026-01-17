package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface AdminUserService {

    Page<User> listUsers(Pageable pageable);

    User setEnabled(Long userId, boolean enabled);

    User setRoles(Long userId, Set<String> roles);
}
