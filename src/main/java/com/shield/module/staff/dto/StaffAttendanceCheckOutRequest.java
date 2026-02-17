package com.shield.module.staff.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record StaffAttendanceCheckOutRequest(
        @NotNull UUID staffId,
        LocalDate attendanceDate
) {
}
