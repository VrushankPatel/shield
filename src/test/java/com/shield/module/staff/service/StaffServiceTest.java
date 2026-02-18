package com.shield.module.staff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.staff.dto.StaffAttendanceSummaryResponse;
import com.shield.module.staff.dto.StaffAttendanceUpdateRequest;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.entity.StaffAttendanceEntity;
import com.shield.module.staff.entity.StaffAttendanceStatus;
import com.shield.module.staff.entity.EmploymentType;
import com.shield.module.staff.entity.StaffEntity;
import com.shield.module.staff.repository.StaffAttendanceRepository;
import com.shield.module.staff.repository.StaffRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    @Test
    void summarizeAttendanceShouldReturnStatusCounts() {
        StaffAttendanceEntity present = new StaffAttendanceEntity();
        present.setStatus(StaffAttendanceStatus.PRESENT);
        present.setCheckInTime(Instant.parse("2026-02-10T03:00:00Z"));
        present.setCheckOutTime(Instant.parse("2026-02-10T11:00:00Z"));

        StaffAttendanceEntity absent = new StaffAttendanceEntity();
        absent.setStatus(StaffAttendanceStatus.ABSENT);

        StaffAttendanceEntity openShift = new StaffAttendanceEntity();
        openShift.setStatus(StaffAttendanceStatus.PRESENT);
        openShift.setCheckInTime(Instant.parse("2026-02-11T03:00:00Z"));
        openShift.setCheckOutTime(null);

        when(staffAttendanceRepository.findAllByAttendanceDateBetweenAndDeletedFalse(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of(present, absent, openShift));

        StaffAttendanceSummaryResponse response = staffService.summarizeAttendance(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28));

        assertEquals(3, response.totalRecords());
        assertEquals(2, response.presentCount());
        assertEquals(1, response.absentCount());
        assertEquals(1, response.openCheckOutCount());
    }

    @Test
    void updateAttendanceShouldRejectCheckoutBeforeCheckin() {
        UUID attendanceId = UUID.randomUUID();
        StaffAttendanceEntity attendance = new StaffAttendanceEntity();
        attendance.setId(attendanceId);
        attendance.setStatus(StaffAttendanceStatus.PRESENT);

        when(staffAttendanceRepository.findByIdAndDeletedFalse(attendanceId)).thenReturn(Optional.of(attendance));

        StaffAttendanceUpdateRequest request = new StaffAttendanceUpdateRequest(
                StaffAttendanceStatus.PRESENT,
                Instant.parse("2026-02-10T11:00:00Z"),
                Instant.parse("2026-02-10T10:00:00Z"));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");

        assertThrows(BadRequestException.class, () -> staffService.updateAttendance(attendanceId, request, principal));
    }
}
