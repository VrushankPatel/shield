package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentVerifyRequest(
        @NotBlank @Size(max = 120) String transactionRef,
        @Size(max = 120) String gatewayPaymentId,
        boolean success,
        @Size(max = 500) String failureReason
) {
}
