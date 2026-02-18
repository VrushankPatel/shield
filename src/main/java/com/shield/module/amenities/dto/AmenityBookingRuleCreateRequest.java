package com.shield.module.amenities.dto;

import jakarta.validation.constraints.NotBlank;

public record AmenityBookingRuleCreateRequest(
        @NotBlank String ruleType,
        @NotBlank String ruleValue,
        Boolean active
) {
}
