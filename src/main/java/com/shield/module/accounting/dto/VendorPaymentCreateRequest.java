package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VendorPaymentCreateRequest(
        @NotNull UUID vendorId,
        UUID expenseId,
        @NotNull LocalDate paymentDate,
        @NotNull @Positive BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        String status) {
}
