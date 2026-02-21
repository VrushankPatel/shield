package com.shield.module.billing.dto;

import com.shield.module.billing.entity.SpecialAssessmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SpecialAssessmentResponse(
        UUID id,
        UUID tenantId,
        String assessmentName,
        String description,
        BigDecimal totalAmount,
        BigDecimal perUnitAmount,
        LocalDate assessmentDate,
        LocalDate dueDate,
        UUID createdBy,
        SpecialAssessmentStatus status) {
}
