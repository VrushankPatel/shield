package com.shield.module.amenities.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record AmenityBookingCreateRequest(
        @NotNull UUID unitId,
        UUID timeSlotId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        LocalDate bookingDate,
        Integer numberOfPersons,
        String purpose,
        BigDecimal bookingAmount,
        BigDecimal securityDeposit,
        String notes
) {
}
