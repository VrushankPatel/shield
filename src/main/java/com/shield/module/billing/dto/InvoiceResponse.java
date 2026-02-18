package com.shield.module.billing.dto;

import com.shield.module.billing.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID tenantId,
        String invoiceNumber,
        UUID unitId,
        UUID billingCycleId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal lateFee,
        BigDecimal gstAmount,
        BigDecimal otherCharges,
        BigDecimal totalAmount,
        BigDecimal outstandingAmount,
        InvoiceStatus status) {
}
