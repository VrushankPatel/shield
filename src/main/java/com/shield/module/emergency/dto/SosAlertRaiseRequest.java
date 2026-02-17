package com.shield.module.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record SosAlertRaiseRequest(
        UUID unitId,
        @NotBlank @Size(max = 100) String alertType,
        @Size(max = 255) String location,
        @Size(max = 2000) String description,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
