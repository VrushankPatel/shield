package com.shield.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportTemplateCreateRequest(
        @NotBlank @Size(max = 255) String templateName,
        @NotBlank @Size(max = 100) String reportType,
        @Size(max = 1000) String description,
        String queryTemplate,
        String parametersJson,
        boolean systemTemplate
) {
}
