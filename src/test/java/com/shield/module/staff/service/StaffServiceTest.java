package com.shield.module.staff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.staff.dto.StaffAttendanceCheckInRequest;
import com.shield.module.staff.dto.StaffAttendanceCheckOutRequest;
import com.shield.module.staff.dto.StaffAttendanceResponse;
import com.shield.module.staff.dto.StaffAttendanceSummaryResponse;
import com.shield.module.staff.dto.StaffAttendanceUpdateRequest;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.dto.StaffStatusUpdateRequest;
import com.shield.module.staff.dto.StaffUpdateRequest;
import com.shield.module.staff.entity.EmploymentType;
import com.shield.module.staff.entity.StaffAttendanceEntity;
import com.shield.module.staff.entity.StaffAttendanceStatus;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    void listEndpointsShouldReturnMappedRows() {
        StaffEntity staff = new StaffEntity();
        staff.setId(UUID.randomUUID());
        staff.setTenantId(UUID.randomUUID());
        staff.setEmployeeId("EMP-001");
        staff.setDesignation("Security Guard");
        staff.setDateOfJoining(LocalDate.of(2026, 1, 1));
        staff.setEmploymentType(EmploymentType.FULL_TIME);
        staff.setBasicSalary(BigDecimal.valueOf(20000));
        staff.setActive(true);

        when(staffRepository.findAllByDeletedFalse(Pageable.ofSize(5))).thenReturn(new PageImpl<>(List.of(staff)));
        when(staffRepository.findAllByActiveTrueAndDeletedFalse(Pageable.ofSize(5))).thenReturn(new PageImpl<>(List.of(staff)));
        when(staffRepository.findAllByDesignationIgnoreCaseAndDeletedFalse("Security Guard", Pageable.ofSize(5)))
                .thenReturn(new PageImpl<>(List.of(staff)));

        PagedResponse<StaffResponse> all = staffService.list(Pageable.ofSize(5));
        PagedResponse<StaffResponse> active = staffService.listActive(Pageable.ofSize(5));
        PagedResponse<StaffResponse> byDesignation = staffService.listByDesignation("Security Guard", Pageable.ofSize(5));

        assertEquals(1, all.content().size());
        assertEquals(1, active.content().size());
        assertEquals(1, byDesignation.content().size());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        UUID staffId = UUID.randomUUID();
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> staffService.getById(staffId));
    }

    @Test
    void updateAndStatusShouldPersistChanges() {
        UUID staffId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        staff.setTenantId(tenantId);
        staff.setEmployeeId("EMP-001");
        staff.setFirstName("Riya");
        staff.setDesignation("Security Guard");
        staff.setDateOfJoining(LocalDate.of(2026, 1, 1));
        staff.setEmploymentType(EmploymentType.FULL_TIME);
        staff.setBasicSalary(BigDecimal.valueOf(18000));
        staff.setActive(true);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
        StaffResponse updated = staffService.update(
                staffId,
                new StaffUpdateRequest(
                        "Riya",
                        "Patel",
                        "8888888888",
                        "riya.patel@shield.dev",
                        "Manager",
                        LocalDate.of(2026, 1, 1),
                        null,
                        EmploymentType.FULL_TIME,
                        BigDecimal.valueOf(22000),
                        true),
                principal);

        assertEquals("Manager", updated.designation());
        assertEquals("Riya", updated.firstName());

        StaffResponse statusUpdated = staffService.updateStatus(staffId, new StaffStatusUpdateRequest(false), principal);
        assertEquals(false, statusUpdated.active());
    }

    @Test
    void deleteShouldSoftDeleteStaff() {
        UUID staffId = UUID.randomUUID();
        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        staff.setTenantId(UUID.randomUUID());
        staff.setDeleted(false);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        staffService.delete(staffId, principal);

        assertTrue(staff.isDeleted());
        verify(staffRepository).save(staff);
    }

    @Test
    void checkInShouldCreateAttendanceWhenMissing() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        LocalDate day = LocalDate.of(2026, 2, 12);

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDateAndDeletedFalse(staffId, day)).thenReturn(Optional.empty());
        when(staffAttendanceRepository.save(any(StaffAttendanceEntity.class))).thenAnswer(invocation -> {
            StaffAttendanceEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
        StaffAttendanceResponse response = staffService.checkIn(new StaffAttendanceCheckInRequest(staffId, day), principal);

        assertEquals(staffId, response.staffId());
        assertEquals(StaffAttendanceStatus.PRESENT, response.status());
        assertEquals(userId, response.markedBy());
    }

    @Test
    void checkOutShouldSetTimesEvenWhenCheckInMissing() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        LocalDate day = LocalDate.of(2026, 2, 12);

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);

        StaffAttendanceEntity attendance = new StaffAttendanceEntity();
        attendance.setId(UUID.randomUUID());
        attendance.setStaffId(staffId);
        attendance.setAttendanceDate(day);
        attendance.setCheckInTime(null);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDateAndDeletedFalse(staffId, day)).thenReturn(Optional.of(attendance));
        when(staffAttendanceRepository.save(any(StaffAttendanceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
        StaffAttendanceResponse response = staffService.checkOut(new StaffAttendanceCheckOutRequest(staffId, day), principal);

        assertEquals(StaffAttendanceStatus.PRESENT, response.status());
        assertEquals(userId, response.markedBy());
    }

    @Test
    void checkOutShouldThrowWhenAttendanceMissing() {
        UUID staffId = UUID.randomUUID();
        LocalDate day = LocalDate.of(2026, 2, 12);

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffAttendanceRepository.findByStaffIdAndAttendanceDateAndDeletedFalse(staffId, day)).thenReturn(Optional.empty());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        StaffAttendanceCheckOutRequest request = new StaffAttendanceCheckOutRequest(staffId, day);
        assertThrows(ResourceNotFoundException.class, () -> staffService.checkOut(request, principal));
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
    void summarizeAttendanceShouldValidateRange() {
        LocalDate from = LocalDate.of(2026, 3, 10);
        LocalDate to = LocalDate.of(2026, 3, 9);
        assertThrows(BadRequestException.class, () -> staffService.summarizeAttendance(from, to));
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

    @Test
    void updateAttendanceShouldRejectCheckOutWhenCheckInMissing() {
        UUID attendanceId = UUID.randomUUID();
        StaffAttendanceEntity attendance = new StaffAttendanceEntity();
        attendance.setId(attendanceId);
        when(staffAttendanceRepository.findByIdAndDeletedFalse(attendanceId)).thenReturn(Optional.of(attendance));

        StaffAttendanceUpdateRequest request = new StaffAttendanceUpdateRequest(
                StaffAttendanceStatus.PRESENT,
                null,
                Instant.parse("2026-02-10T10:00:00Z"));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        assertThrows(BadRequestException.class, () -> staffService.updateAttendance(attendanceId, request, principal));
    }

    @Test
    void listAttendanceByDateRangeShouldValidateDates() {
        LocalDate from = LocalDate.of(2026, 3, 10);
        LocalDate to = LocalDate.of(2026, 3, 1);
        Pageable pageable = Pageable.ofSize(10);
        assertThrows(BadRequestException.class, () -> staffService.listAttendanceByDateRange(from, to, pageable));
    }
}
