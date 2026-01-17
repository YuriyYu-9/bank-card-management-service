package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UserRolesUpdateRequest(
        @NotNull(message = "roles must not be null")
        @NotEmpty(message = "roles must not be empty")
        @ArraySchema(schema = @Schema(
                example = "USER",
                allowableValues = {"USER", "ADMIN"}
        ))
        Set<String> roles
) {}
