package com.shield.module.visitor.dto;

import com.shield.module.visitor.entity.VisitorPassStatus;
import java.time.Instant;
import java.util.UUID;

public record VisitorPassResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        String visitorName,
        String vehicleNumber,
        Instant validFrom,
        Instant validTo,
        String qrCode,
        VisitorPassStatus status
) {
}
