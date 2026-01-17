package com.example.bankcards.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

final class TestAuthHelper {

    private TestAuthHelper() {
    }

    static void setAuth(String username) {
        var auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    static void clear() {
        SecurityContextHolder.clearContext();
    }
}
