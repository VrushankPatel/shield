package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DieselGeneratorResponse(
        UUID id,
        UUID tenantId,
        String generatorName,
        BigDecimal capacityKva,
        String location
) {
}
