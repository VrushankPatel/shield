package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentCallbackRequest(
        @NotBlank @Size(max = 120) String transactionRef,
        @Size(max = 120) String gatewayOrderId,
        @Size(max = 120) String gatewayPaymentId,
        @NotBlank @Size(max = 32) String status,
        String payload,
        @Size(max = 256) String signature
) {
}
