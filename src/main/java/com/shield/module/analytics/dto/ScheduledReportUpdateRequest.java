package com.shield.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ScheduledReportUpdateRequest(
        @NotBlank @Size(max = 255) String reportName,
        @NotBlank @Size(max = 50) String frequency,
        @Size(max = 2000) String recipients,
        Instant nextGenerationAt
) {
}
