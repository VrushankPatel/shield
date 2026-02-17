package com.shield.module.analytics.dto;

public record StaffAttendanceSummaryResponse(
        long totalStaff,
        long presentToday,
        long absentToday
) {
}
