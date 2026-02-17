package com.shield.module.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID tenantId,
        UUID billId,
        BigDecimal amount,
        String mode,
        String transactionRef,
        Instant paidAt
) {
}
