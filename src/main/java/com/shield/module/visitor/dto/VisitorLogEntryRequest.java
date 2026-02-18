package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VisitorLogEntryRequest(
        @NotNull UUID visitorPassId,
        String entryGate,
        UUID securityGuardEntry,
        String faceCaptureUrl
) {
}
