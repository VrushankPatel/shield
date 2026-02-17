package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record VisitorPassCreateRequest(
        @NotNull UUID unitId,
        @NotBlank String visitorName,
        String vehicleNumber,
        @NotNull Instant validFrom,
        @NotNull Instant validTo
) {
}
