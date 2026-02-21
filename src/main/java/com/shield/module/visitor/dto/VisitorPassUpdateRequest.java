package com.shield.module.visitor.dto;

import com.shield.module.visitor.entity.VisitorPassStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VisitorPassUpdateRequest(
        UUID visitorId,
        @NotNull UUID unitId,
        String visitorName,
        String vehicleNumber,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        LocalDate visitDate,
        String purpose,
        Integer numberOfPersons,
        VisitorPassStatus status
) {
}
