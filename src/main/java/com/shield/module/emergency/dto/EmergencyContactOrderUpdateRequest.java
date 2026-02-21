package com.shield.module.emergency.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EmergencyContactOrderUpdateRequest(
        @NotNull @Min(0) Integer displayOrder
) {
}
