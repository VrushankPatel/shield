package com.shield.module.utility.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ElectricityMeterUpdateRequest(
        @NotBlank @Size(max = 50) String meterType,
        @Size(max = 255) String location,
        UUID unitId
) {
}
