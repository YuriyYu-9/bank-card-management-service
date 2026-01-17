package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        @Schema(example = "2")
        Long id,

        @Schema(example = "user")
        String username,

        @Schema(example = "true")
        boolean enabled,

        @Schema(example = "[\"USER\"]")
        Set<String> roles,

        @Schema(example = "2026-01-17T09:00:00")
        LocalDateTime createdAt
) {}
