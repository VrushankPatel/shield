package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.ExpensePaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID tenantId,
        String expenseNumber,
        UUID accountHeadId,
        UUID fundCategoryId,
        UUID vendorId,
        LocalDate expenseDate,
        BigDecimal amount,
        String description,
        String invoiceNumber,
        String invoiceUrl,
        ExpensePaymentStatus paymentStatus,
        UUID approvedBy,
        LocalDate approvalDate) {
}
