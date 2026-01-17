package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferCreateRequest(
        @NotNull(message = "fromCardId must not be null")
        @Schema(example = "10")
        Long fromCardId,

        @NotNull(message = "toCardId must not be null")
        @Schema(example = "11")
        Long toCardId,

        @Positive(message = "amountCents must be > 0")
        @Schema(example = "1500", description = "Amount in minor units (cents)")
        long amountCents
) {}
