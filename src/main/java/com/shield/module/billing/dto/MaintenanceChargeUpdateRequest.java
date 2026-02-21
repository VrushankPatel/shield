package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MaintenanceChargeUpdateRequest(
        BigDecimal baseAmount,
        String calculationMethod,
        BigDecimal areaBasedAmount,
        BigDecimal fixedAmount,
        @NotNull @Positive BigDecimal totalAmount) {
}
