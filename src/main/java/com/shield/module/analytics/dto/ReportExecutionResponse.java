package com.shield.module.analytics.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ReportExecutionResponse(
        UUID templateId,
        String templateName,
        String reportType,
        Instant generatedAt,
        Map<String, Object> data
) {
}
