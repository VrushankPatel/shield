package com.shield.module.utility.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ElectricityMeterCreateRequest(
        @NotBlank @Size(max = 100) String meterNumber,
        @NotBlank @Size(max = 50) String meterType,
        @Size(max = 255) String location,
        UUID unitId
) {
}
