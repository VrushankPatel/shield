package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull UUID billId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String mode,
        String transactionRef
) {
}
