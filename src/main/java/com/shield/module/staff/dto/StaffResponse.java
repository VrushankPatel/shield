package com.shield.module.staff.dto;

import com.shield.module.staff.entity.EmploymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        UUID tenantId,
        String employeeId,
        String firstName,
        String lastName,
        String phone,
        String email,
        String designation,
        LocalDate dateOfJoining,
        LocalDate dateOfLeaving,
        EmploymentType employmentType,
        BigDecimal basicSalary,
        boolean active
) {
}
