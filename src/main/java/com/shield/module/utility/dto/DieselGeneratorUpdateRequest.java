package com.shield.module.utility.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DieselGeneratorUpdateRequest(
        @NotBlank @Size(max = 100) String generatorName,
        @NotNull @DecimalMin("0.0") BigDecimal capacityKva,
        @Size(max = 255) String location
) {
}
