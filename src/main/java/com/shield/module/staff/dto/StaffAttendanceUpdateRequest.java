package com.shield.module.staff.dto;

import com.shield.module.staff.entity.StaffAttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record StaffAttendanceUpdateRequest(
        @NotNull StaffAttendanceStatus status,
        Instant checkInTime,
        Instant checkOutTime
) {
}
