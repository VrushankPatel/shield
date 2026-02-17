package com.shield.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record ScheduledReportCreateRequest(
        @NotNull UUID templateId,
        @NotBlank @Size(max = 255) String reportName,
        @NotBlank @Size(max = 50) String frequency,
        @Size(max = 2000) String recipients,
        Instant nextGenerationAt,
        Boolean active
) {
}
