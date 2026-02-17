package com.shield.module.utility.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record WaterTankUpdateRequest(
        @NotBlank @Size(max = 100) String tankName,
        @NotBlank @Size(max = 50) String tankType,
        @NotNull @DecimalMin("0.0") BigDecimal capacity,
        @Size(max = 255) String location
) {
}
