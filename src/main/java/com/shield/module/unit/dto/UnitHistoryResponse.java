package com.shield.module.unit.dto;

import com.shield.module.move.entity.MoveStatus;
import com.shield.module.move.entity.MoveType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UnitHistoryResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        UUID userId,
        MoveType moveType,
        MoveStatus status,
        LocalDate effectiveDate,
        LocalDate approvalDate,
        String decisionNotes,
        Instant createdAt
) {
}
