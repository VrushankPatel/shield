package com.shield.module.visitor.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VisitorLogExitRequest(
        @NotNull UUID visitorPassId,
        String exitGate,
        UUID securityGuardExit
) {
}
