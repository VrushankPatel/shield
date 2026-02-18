package com.shield.module.digitalid.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DigitalIdRenewRequest(
        @Min(1) @Max(3650) Integer validityDays,
        @Size(max = 2000) String qrCodeUrl
) {
}
