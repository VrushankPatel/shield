package com.shield.module.staff.dto;

import com.shield.module.staff.entity.StaffAttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StaffAttendanceResponse(
        UUID id,
        UUID tenantId,
        UUID staffId,
        LocalDate attendanceDate,
        Instant checkInTime,
        Instant checkOutTime,
        StaffAttendanceStatus status,
        UUID markedBy
) {
}
