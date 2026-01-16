package com.example.bankcards.dto;

import jakarta.validation.constraints.*;

public record CardCreateRequest(
        @NotBlank(message = "pan must not be blank")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "pan must contain 13..19 digits")
        String pan,

        @Min(value = 1, message = "expiryMonth must be 1..12")
        @Max(value = 12, message = "expiryMonth must be 1..12")
        int expiryMonth,

        @Min(value = 2000, message = "expiryYear is too small")
        @Max(value = 2100, message = "expiryYear is too large")
        int expiryYear,

        @NotBlank(message = "currency must not be blank")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO-4217 (e.g. USD)")
        String currency,

        @Min(value = 0, message = "initialBalanceCents must be >= 0")
        long initialBalanceCents,

        @NotNull(message = "ownerUserId must not be null")
        Long ownerUserId
) {}
