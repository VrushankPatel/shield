package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiateRequest(
        @NotNull UUID billId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(max = 50) String mode,
        @Size(max = 50) String provider
) {
}
