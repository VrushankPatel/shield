package com.shield.module.billing.dto;

import com.shield.module.billing.entity.BillingCycleStatus;
import java.time.LocalDate;
import java.util.UUID;

public record BillingCycleResponse(
        UUID id,
        UUID tenantId,
        String cycleName,
        Integer month,
        Integer year,
        LocalDate dueDate,
        LocalDate lateFeeApplicableDate,
        BillingCycleStatus status) {
}
