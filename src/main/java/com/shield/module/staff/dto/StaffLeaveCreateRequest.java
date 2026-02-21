package com.shield.module.staff.dto;

import com.shield.module.staff.entity.StaffLeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record StaffLeaveCreateRequest(
        @NotNull UUID staffId,
        @NotNull StaffLeaveType leaveType,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @Min(1) int numberOfDays,
        @Size(max = 2000) String reason
) {
}
