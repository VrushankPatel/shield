package com.shield.module.visitor.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record DomesticHelpUpdateRequest(
        @NotBlank String helpName,
        String phone,
        String helpType,
        Boolean permanentPass,
        Boolean policeVerificationDone,
        LocalDate verificationDate,
        String photoUrl
) {
}
