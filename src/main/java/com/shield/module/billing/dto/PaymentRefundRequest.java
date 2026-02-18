package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRefundRequest(@NotBlank String reason) {
}
