package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CardResponse(
        @Schema(example = "10")
        Long id,

        @Schema(example = "**** **** **** 4242")
        String panMasked,

        @Schema(example = "12")
        int expiryMonth,

        @Schema(example = "2030")
        int expiryYear,

        @Schema(example = "ACTIVE", description = "Computed API state: ACTIVE/BLOCKED/EXPIRED")
        CardState status,

        @Schema(example = "48500", description = "Balance in minor units (cents)")
        long balanceCents,

        @Schema(example = "USD")
        String currency,

        @Schema(example = "2026-01-17T10:15:30")
        LocalDateTime createdAt
) {}
