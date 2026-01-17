package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;

public record UserEnabledUpdateRequest(
        @NotNull(message = "enabled must not be null")
        Boolean enabled
) {}
