package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        String panMasked,
        int expiryMonth,
        int expiryYear,
        CardStatus status,
        long balanceCents,
        String currency,
        LocalDateTime createdAt
) {}
