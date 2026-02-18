package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record DeliveryLogCreateRequest(
        @NotNull UUID unitId,
        @NotBlank String deliveryPartner,
        String trackingNumber,
        Instant deliveryTime,
        UUID receivedBy,
        UUID securityGuardId,
        String photoUrl
) {
}
