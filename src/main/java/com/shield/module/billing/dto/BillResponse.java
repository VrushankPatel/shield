package com.shield.module.billing.dto;

import com.shield.module.billing.entity.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BillResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        Integer month,
        Integer year,
        BigDecimal amount,
        LocalDate dueDate,
        BillStatus status,
        BigDecimal lateFee
) {
}
