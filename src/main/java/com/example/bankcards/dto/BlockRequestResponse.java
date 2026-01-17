package com.example.bankcards.dto;

import com.example.bankcards.entity.BlockRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record BlockRequestResponse(
        @Schema(example = "200")
        Long id,

        @Schema(example = "10")
        Long cardId,

        @Schema(example = "2")
        Long requestedByUserId,

        @Schema(example = "PENDING")
        BlockRequestStatus status,

        @Schema(example = "2026-01-17T10:45:00")
        LocalDateTime createdAt
) {}
