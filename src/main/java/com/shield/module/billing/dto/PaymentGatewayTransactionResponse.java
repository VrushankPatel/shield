package com.shield.module.billing.dto;

import com.shield.module.billing.entity.PaymentGatewayTransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentGatewayTransactionResponse(
        UUID id,
        UUID tenantId,
        UUID billId,
        String transactionRef,
        String provider,
        String gatewayOrderId,
        String gatewayPaymentId,
        BigDecimal amount,
        String currency,
        String mode,
        PaymentGatewayTransactionStatus status,
        String failureReason,
        UUID paymentId,
        Instant verifiedAt,
        Instant createdAt
) {
}
