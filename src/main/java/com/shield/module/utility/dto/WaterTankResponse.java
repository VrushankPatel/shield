package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WaterTankResponse(
        UUID id,
        UUID tenantId,
        String tankName,
        String tankType,
        BigDecimal capacity,
        String location
) {
}
