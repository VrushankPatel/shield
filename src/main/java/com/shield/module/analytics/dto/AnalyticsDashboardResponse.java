package com.shield.module.analytics.dto;

import java.util.UUID;

public record AnalyticsDashboardResponse(
        UUID id,
        UUID tenantId,
        String dashboardName,
        String dashboardType,
        String widgetsJson,
        UUID createdBy,
        boolean defaultDashboard
) {
}
