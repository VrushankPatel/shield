package com.shield.module.analytics.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportTemplateResponse(
        UUID id,
        UUID tenantId,
        String templateName,
        String reportType,
        String description,
        String queryTemplate,
        String parametersJson,
        UUID createdBy,
        boolean systemTemplate,
        Instant createdAt
) {
}
