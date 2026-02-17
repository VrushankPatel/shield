package com.shield.module.amenities.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmenityCreateRequest(
        @NotBlank String name,
        @NotNull @Min(1) Integer capacity,
        boolean requiresApproval
) {
}
