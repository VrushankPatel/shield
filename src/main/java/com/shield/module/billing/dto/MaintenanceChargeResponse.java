package com.shield.module.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceChargeResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        UUID billingCycleId,
        BigDecimal baseAmount,
        String calculationMethod,
        BigDecimal areaBasedAmount,
        BigDecimal fixedAmount,
        BigDecimal totalAmount) {
}
