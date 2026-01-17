package com.example.bankcards.dto;

import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        String panMasked,
        int expiryMonth,
        int expiryYear,
        CardState status,
        long balanceCents,
        String currency,
        LocalDateTime createdAt
) {}
