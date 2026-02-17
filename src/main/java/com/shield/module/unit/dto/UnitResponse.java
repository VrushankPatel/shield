package com.shield.module.unit.dto;

import com.shield.module.unit.entity.UnitStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        UUID tenantId,
        String unitNumber,
        String block,
        String type,
        BigDecimal squareFeet,
        UnitStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
