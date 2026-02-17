package com.shield.module.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyticsDashboardUpdateRequest(
        @NotBlank @Size(max = 255) String dashboardName,
        @NotBlank @Size(max = 100) String dashboardType,
        String widgetsJson,
        boolean defaultDashboard
) {
}
