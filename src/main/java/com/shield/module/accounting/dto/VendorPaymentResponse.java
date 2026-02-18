package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.VendorPaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VendorPaymentResponse(
        UUID id,
        UUID tenantId,
        UUID vendorId,
        UUID expenseId,
        LocalDate paymentDate,
        BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        UUID createdBy,
        VendorPaymentStatus status) {
}
