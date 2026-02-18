package com.shield.module.amenities.dto;

import com.shield.module.amenities.entity.AmenityBookingStatus;
import com.shield.module.amenities.entity.AmenityPaymentStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record AmenityBookingUpdateRequest(
        @NotNull UUID unitId,
        UUID timeSlotId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        LocalDate bookingDate,
        Integer numberOfPersons,
        String purpose,
        BigDecimal bookingAmount,
        BigDecimal securityDeposit,
        AmenityBookingStatus status,
        AmenityPaymentStatus paymentStatus,
        String notes
) {
}
