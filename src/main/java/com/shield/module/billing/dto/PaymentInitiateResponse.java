package com.shield.module.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentInitiateResponse(
        String transactionRef,
        String gatewayOrderId,
        String provider,
        BigDecimal amount,
        String currency,
        String checkoutToken,
        Instant expiresAt
) {
}
