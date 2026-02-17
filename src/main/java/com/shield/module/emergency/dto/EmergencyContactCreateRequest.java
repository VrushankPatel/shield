package com.shield.module.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmergencyContactCreateRequest(
        @NotBlank @Size(max = 100) String contactType,
        @NotBlank @Size(max = 255) String contactName,
        @NotBlank @Size(max = 20) String phonePrimary,
        @Size(max = 20) String phoneSecondary,
        @Size(max = 500) String address,
        Integer displayOrder,
        boolean active
) {
}
