package com.shield.module.visitor.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record DomesticHelpCreateRequest(
        @NotBlank String helpName,
        String phone,
        String helpType,
        Boolean permanentPass,
        Boolean policeVerificationDone,
        LocalDate verificationDate,
        String photoUrl,
        UUID registeredBy
) {
}
