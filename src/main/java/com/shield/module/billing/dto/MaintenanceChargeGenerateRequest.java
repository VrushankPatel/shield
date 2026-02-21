package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceChargeGenerateRequest(
        @NotNull UUID unitId,
        @NotNull UUID billingCycleId,
        BigDecimal baseAmount,
        String calculationMethod,
        BigDecimal areaBasedAmount,
        BigDecimal fixedAmount,
        @NotNull @Positive BigDecimal totalAmount) {
}
