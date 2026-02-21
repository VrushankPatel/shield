package com.shield.module.staff.dto;

import java.util.UUID;

public record StaffLeaveBalanceResponse(
        UUID staffId,
        int totalRequests,
        int approvedDays,
        int pendingDays,
        int rejectedDays
) {
}
