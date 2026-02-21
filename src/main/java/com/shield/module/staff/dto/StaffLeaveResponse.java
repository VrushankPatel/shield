package com.shield.module.staff.dto;

import com.shield.module.staff.entity.StaffLeaveStatus;
import com.shield.module.staff.entity.StaffLeaveType;
import java.time.LocalDate;
import java.util.UUID;

public record StaffLeaveResponse(
        UUID id,
        UUID tenantId,
        UUID staffId,
        StaffLeaveType leaveType,
        LocalDate fromDate,
        LocalDate toDate,
        int numberOfDays,
        String reason,
        StaffLeaveStatus status,
        UUID approvedBy,
        LocalDate approvalDate
) {
}
