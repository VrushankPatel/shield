package com.shield.module.visitor.dto;

import java.time.Instant;
import java.util.UUID;

public record VisitorLogResponse(
        UUID id,
        UUID tenantId,
        UUID visitorPassId,
        Instant entryTime,
        Instant exitTime,
        String entryGate,
        String exitGate,
        UUID securityGuardEntry,
        UUID securityGuardExit,
        String faceCaptureUrl
) {
}
