package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record TransferResponse(
        @Schema(example = "100")
        Long id,

        @Schema(example = "10")
        Long fromCardId,

        @Schema(example = "11")
        Long toCardId,

        @Schema(example = "1500")
        long amountCents,

        @Schema(example = "USD")
        String currency,

        @Schema(example = "2026-01-17T10:30:00")
        LocalDateTime createdAt
) {}
