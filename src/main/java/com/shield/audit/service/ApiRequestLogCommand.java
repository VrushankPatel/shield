package com.shield.audit.service;

import java.util.UUID;

public record ApiRequestLogCommand(
        String requestId,
        UUID tenantId,
        UUID userId,
        String endpoint,
        String httpMethod,
        String requestBody,
        Integer responseStatus,
        Long responseTimeMs,
        String ipAddress,
        String userAgent) {}
