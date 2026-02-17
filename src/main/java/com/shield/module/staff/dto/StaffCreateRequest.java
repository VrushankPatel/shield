package com.shield.module.staff.dto;

import com.shield.module.staff.entity.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StaffCreateRequest(
        @NotBlank @Size(max = 50) String employeeId,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 20) String phone,
        @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String designation,
        @NotNull LocalDate dateOfJoining,
        LocalDate dateOfLeaving,
        @NotNull EmploymentType employmentType,
        @NotNull @DecimalMin("0.0") BigDecimal basicSalary,
        boolean active
) {
}
