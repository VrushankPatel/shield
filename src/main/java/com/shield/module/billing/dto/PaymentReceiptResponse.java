package com.shield.module.billing.dto;

import java.util.UUID;

public record PaymentReceiptResponse(
        UUID paymentId,
        String receiptUrl) {
}
