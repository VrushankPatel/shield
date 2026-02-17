package com.shield.module.emergency.dto;

import com.shield.module.emergency.entity.SosAlertStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SosAlertResponse(
        UUID id,
        UUID tenantId,
        String alertNumber,
        UUID raisedBy,
        UUID unitId,
        String alertType,
        String location,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        SosAlertStatus status,
        UUID respondedBy,
        Instant respondedAt,
        Instant resolvedAt
) {
}
