package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VisitorUpdateRequest(
        @NotBlank String visitorName,
        @NotBlank @Pattern(regexp = "^[0-9+\\-() ]{7,20}$") String phone,
        String vehicleNumber,
        String visitorType,
        String idProofType,
        String idProofNumber,
        String photoUrl
) {
}
