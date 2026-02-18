package com.shield.module.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.payroll.dto.PayrollGenerateRequest;
import com.shield.module.payroll.dto.PayrollProcessRequest;
import com.shield.module.payroll.dto.PayrollResponse;
import com.shield.module.payroll.entity.PayrollEntity;
import com.shield.module.payroll.entity.PayrollStatus;
import com.shield.module.payroll.repository.PayrollRepository;
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

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffAttendanceRepository staffAttendanceRepository;

    @Mock
    private AuditLogService auditLogService;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollService(payrollRepository, staffRepository, staffAttendanceRepository, auditLogService);
    }

    @Test
    void generateShouldCalculateGrossAndNetSalaryFromAttendance() {
        UUID staffId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        staff.setTenantId(tenantId);
        staff.setBasicSalary(BigDecimal.valueOf(30000));

        List<StaffAttendanceEntity> attendance = List.of(
                attendance(staffId, LocalDate.of(2026, 2, 1)),
                attendance(staffId, LocalDate.of(2026, 2, 2)),
                attendance(staffId, LocalDate.of(2026, 2, 3)),
                attendance(staffId, LocalDate.of(2026, 2, 4)),
                attendance(staffId, LocalDate.of(2026, 2, 5)),
                attendance(staffId, LocalDate.of(2026, 2, 6)),
                attendance(staffId, LocalDate.of(2026, 2, 7)),
                attendance(staffId, LocalDate.of(2026, 2, 8)),
                attendance(staffId, LocalDate.of(2026, 2, 9)),
                attendance(staffId, LocalDate.of(2026, 2, 10)),
                attendance(staffId, LocalDate.of(2026, 2, 11)),
                attendance(staffId, LocalDate.of(2026, 2, 12)),
                attendance(staffId, LocalDate.of(2026, 2, 13)),
                attendance(staffId, LocalDate.of(2026, 2, 14)),
                attendance(staffId, LocalDate.of(2026, 2, 15)),
                attendance(staffId, LocalDate.of(2026, 2, 16)),
                attendance(staffId, LocalDate.of(2026, 2, 17)),
                attendance(staffId, LocalDate.of(2026, 2, 18)),
                attendance(staffId, LocalDate.of(2026, 2, 19)),
                attendance(staffId, LocalDate.of(2026, 2, 20))
        );

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffAttendanceRepository.findAllByStaffIdAndAttendanceDateBetweenAndDeletedFalse(
                any(UUID.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(attendance);
        when(payrollRepository.findByStaffIdAndYearAndMonthAndDeletedFalse(staffId, 2026, 2)).thenReturn(Optional.empty());
        when(payrollRepository.save(any(PayrollEntity.class))).thenAnswer(invocation -> {
            PayrollEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PayrollGenerateRequest request = new PayrollGenerateRequest(
                staffId,
                2,
                2026,
                30,
                BigDecimal.valueOf(1000),
                null,
                null);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");

        PayrollResponse response = payrollService.generate(request, principal);

        assertEquals(20, response.presentDays());
        assertEquals(BigDecimal.valueOf(20000).setScale(2), response.grossSalary());
        assertEquals(BigDecimal.valueOf(19000).setScale(2), response.netSalary());
    }

    @Test
    void processShouldMovePayrollToProcessedWithPaymentDetails() {
        UUID payrollId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        PayrollEntity entity = new PayrollEntity();
        entity.setId(payrollId);
        entity.setTenantId(tenantId);
        entity.setStatus(PayrollStatus.DRAFT);

        when(payrollRepository.findByIdAndDeletedFalse(payrollId)).thenReturn(Optional.of(entity));
        when(payrollRepository.save(any(PayrollEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollProcessRequest request = new PayrollProcessRequest(
                payrollId,
                "BANK_TRANSFER",
                "PAY-123",
                LocalDate.of(2026, 2, 28),
                "https://files.example/payslip.pdf");
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");

        PayrollResponse response = payrollService.process(request, principal);

        assertEquals(PayrollStatus.PROCESSED, response.status());
        assertEquals("BANK_TRANSFER", response.paymentMethod());
        assertEquals("PAY-123", response.paymentReference());
    }

    @Test
    void approveShouldRejectDraftPayroll() {
        UUID payrollId = UUID.randomUUID();

        PayrollEntity entity = new PayrollEntity();
        entity.setId(payrollId);
        entity.setStatus(PayrollStatus.DRAFT);

        when(payrollRepository.findByIdAndDeletedFalse(payrollId)).thenReturn(Optional.of(entity));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");

        assertThrows(BadRequestException.class, () -> payrollService.approve(payrollId, principal));
    }

    private StaffAttendanceEntity attendance(UUID staffId, LocalDate date) {
        StaffAttendanceEntity entity = new StaffAttendanceEntity();
        entity.setStaffId(staffId);
        entity.setAttendanceDate(date);
        entity.setStatus(StaffAttendanceStatus.PRESENT);
        entity.setCheckInTime(Instant.parse("2026-02-17T10:15:30Z"));
        return entity;
    }
}
