package com.shield.module.move.dto;

import com.shield.module.move.entity.MoveStatus;
import com.shield.module.move.entity.MoveType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record MoveRecordResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        UUID userId,
        MoveType moveType,
        LocalDate effectiveDate,
        BigDecimal securityDeposit,
        String agreementUrl,
        MoveStatus status,
        String decisionNotes,
        UUID approvedBy,
        LocalDate approvalDate,
        Instant createdAt
) {
}
