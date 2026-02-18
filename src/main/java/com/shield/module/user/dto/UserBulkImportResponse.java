package com.shield.module.user.dto;

import java.util.List;

public record UserBulkImportResponse(
        int totalRequested,
        int createdCount,
        List<String> errors
) {
}
