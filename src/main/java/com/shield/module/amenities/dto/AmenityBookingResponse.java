package com.shield.module.amenities.dto;

import com.shield.module.amenities.entity.AmenityBookingStatus;
import com.shield.module.amenities.entity.AmenityPaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record AmenityBookingResponse(
        UUID id,
        UUID tenantId,
        String bookingNumber,
        UUID amenityId,
        UUID timeSlotId,
        UUID unitId,
        UUID bookedBy,
        LocalDate bookingDate,
        Instant startTime,
        Instant endTime,
        Integer numberOfPersons,
        String purpose,
        AmenityBookingStatus status,
        BigDecimal bookingAmount,
        BigDecimal securityDeposit,
        AmenityPaymentStatus paymentStatus,
        UUID approvedBy,
        Instant approvalDate,
        Instant cancellationDate,
        String cancellationReason,
        String notes
) {
}
