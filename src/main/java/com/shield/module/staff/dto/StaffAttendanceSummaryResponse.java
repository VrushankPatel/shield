package com.shield.module.staff.dto;

import java.time.LocalDate;

public record StaffAttendanceSummaryResponse(
        LocalDate fromDate,
        LocalDate toDate,
        long totalRecords,
        long presentCount,
        long absentCount,
        long halfDayCount,
        long leaveCount,
        long openCheckOutCount
) {
}
