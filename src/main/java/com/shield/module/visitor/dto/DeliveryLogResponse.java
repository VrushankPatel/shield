package com.shield.module.visitor.dto;

import java.time.Instant;
import java.util.UUID;

public record DeliveryLogResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        String deliveryPartner,
        String trackingNumber,
        Instant deliveryTime,
        UUID receivedBy,
        UUID securityGuardId,
        String photoUrl
) {
}
