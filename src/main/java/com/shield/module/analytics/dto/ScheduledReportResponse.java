package com.shield.module.analytics.dto;

import java.time.Instant;
import java.util.UUID;

public record ScheduledReportResponse(
        UUID id,
        UUID tenantId,
        UUID templateId,
        String reportName,
        String frequency,
        String recipients,
        boolean active,
        Instant lastGeneratedAt,
        Instant nextGenerationAt
) {
}
