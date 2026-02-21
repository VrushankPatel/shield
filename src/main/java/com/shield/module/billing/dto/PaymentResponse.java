package com.shield.module.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID tenantId,
        UUID billId,
        UUID invoiceId,
        UUID unitId,
        BigDecimal amount,
        String mode,
        String paymentStatus,
        String transactionRef,
        String receiptUrl,
        Instant paidAt,
        Instant refundedAt
) {
}
