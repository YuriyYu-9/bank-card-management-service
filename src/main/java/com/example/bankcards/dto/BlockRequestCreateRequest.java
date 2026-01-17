package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BlockRequestCreateRequest(
        @NotNull(message = "cardId must not be null")
        @Schema(example = "10")
        Long cardId
) {}
