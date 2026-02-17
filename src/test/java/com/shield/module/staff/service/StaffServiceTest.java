package com.shield.module.staff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.entity.EmploymentType;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.repository.StaffAttendanceRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;

    @Mock
    private AuditLogService auditLogService;

    private StaffService staffService;

    @BeforeEach
    void setUp() {
        staffService = new StaffService(staffRepository, staffAttendanceRepository, auditLogService);
    }

    @Test
    void createShouldSetTenantFromPrincipal() {
        when(staffRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> {
            StaffEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        StaffCreateRequest request = new StaffCreateRequest(
                "EMP-001",
                "Riya",
                "Shah",
                "9999999999",
                "riya@shield.dev",
                "Security Guard",
                LocalDate.of(2026, 1, 1),
                null,
                EmploymentType.FULL_TIME,
                BigDecimal.valueOf(18000),
                true);

        StaffResponse response = staffService.create(request, principal);

        assertEquals(principal.tenantId(), response.tenantId());
        assertEquals("EMP-001", response.employeeId());
        assertEquals("Riya", response.firstName());
    }
}
