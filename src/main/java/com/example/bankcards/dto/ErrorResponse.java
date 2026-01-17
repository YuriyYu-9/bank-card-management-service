package com.example.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Unified error response format")
public record ErrorResponse(

        @Schema(example = "VALIDATION_ERROR")
        String error,

        @Schema(example = "Validation failed: field: message")
        String message,

        @Schema(example = "2026-01-17T10:00:00Z")
        Instant timestamp,

        @Schema(example = "/api/cards/my")
        String path
) {
    public static ErrorResponse of(String error, String message, String path) {
        return new ErrorResponse(error, message, Instant.now(), path);
    }
}
