package com.shield.module.visitor.dto;

import com.shield.module.visitor.entity.VisitorPassStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VisitorPassResponse(
        UUID id,
        UUID tenantId,
        String passNumber,
        UUID visitorId,
        UUID unitId,
        String visitorName,
        String vehicleNumber,
        LocalDate visitDate,
        Instant validFrom,
        Instant validTo,
        String qrCode,
        String purpose,
        Integer numberOfPersons,
        UUID approvedBy,
        VisitorPassStatus status
) {
}
