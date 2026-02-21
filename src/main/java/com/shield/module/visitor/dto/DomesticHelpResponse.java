package com.shield.module.visitor.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DomesticHelpResponse(
        UUID id,
        UUID tenantId,
        String helpName,
        String phone,
        String helpType,
        boolean permanentPass,
        boolean policeVerificationDone,
        LocalDate verificationDate,
        String photoUrl,
        UUID registeredBy
) {
}
