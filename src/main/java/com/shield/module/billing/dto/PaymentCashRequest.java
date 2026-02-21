package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCashRequest(
        @NotNull UUID invoiceId,
        @NotNull @Positive BigDecimal amount,
        String transactionRef) {
}
