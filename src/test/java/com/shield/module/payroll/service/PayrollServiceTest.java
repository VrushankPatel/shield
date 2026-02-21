package com.shield.module.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.payroll.dto.PayrollBulkProcessRequest;
import com.shield.module.payroll.dto.PayrollGenerateRequest;
import com.shield.module.payroll.dto.PayrollPayslipResponse;
import com.shield.module.payroll.dto.PayrollProcessRequest;
import com.shield.module.payroll.dto.PayrollResponse;
import com.shield.module.payroll.entity.PayrollComponentEntity;
import com.shield.module.payroll.entity.PayrollComponentType;
import com.shield.module.payroll.entity.PayrollDetailEntity;
import com.shield.module.payroll.entity.PayrollEntity;
import com.shield.module.payroll.entity.PayrollStatus;
import com.shield.module.payroll.entity.StaffSalaryStructureEntity;
import com.shield.module.payroll.repository.PayrollComponentRepository;
import com.shield.module.payroll.repository.PayrollDetailRepository;
import com.shield.module.payroll.repository.PayrollRepository;
import com.shield.module.payroll.repository.StaffSalaryStructureRepository;
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
    private StaffSalaryStructureRepository staffSalaryStructureRepository;

    @Mock
    private PayrollComponentRepository payrollComponentRepository;

    @Mock
    private PayrollDetailRepository payrollDetailRepository;

    @Mock
    private AuditLogService auditLogService;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollService(
                payrollRepository,
                staffRepository,
                staffAttendanceRepository,
                staffSalaryStructureRepository,
                payrollComponentRepository,
                payrollDetailRepository,
                auditLogService);
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
        when(staffSalaryStructureRepository.findAllByStaffIdAndActiveTrueAndEffectiveFromLessThanEqualAndDeletedFalse(
                any(UUID.class), any(LocalDate.class))).thenReturn(List.of());
        when(payrollRepository.findByStaffIdAndYearAndMonthAndDeletedFalse(staffId, 2026, 2)).thenReturn(Optional.empty());
        when(payrollRepository.save(any(PayrollEntity.class))).thenAnswer(invocation -> {
            PayrollEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(payrollDetailRepository.findAllByPayrollIdAndDeletedFalse(any(UUID.class))).thenReturn(List.of());

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
    void generateWithSalaryStructureShouldPersistPayrollDetails() {
        UUID staffId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID earningComponentId = UUID.randomUUID();
        UUID deductionComponentId = UUID.randomUUID();

        StaffEntity staff = new StaffEntity();
        staff.setId(staffId);
        staff.setTenantId(tenantId);
        staff.setBasicSalary(BigDecimal.valueOf(20000));

        StaffSalaryStructureEntity earning = new StaffSalaryStructureEntity();
        earning.setStaffId(staffId);
        earning.setPayrollComponentId(earningComponentId);
        earning.setAmount(BigDecimal.valueOf(30000));
        earning.setActive(true);
        earning.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        StaffSalaryStructureEntity deduction = new StaffSalaryStructureEntity();
        deduction.setStaffId(staffId);
        deduction.setPayrollComponentId(deductionComponentId);
        deduction.setAmount(BigDecimal.valueOf(1500));
        deduction.setActive(true);
        deduction.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        PayrollComponentEntity earningComponent = new PayrollComponentEntity();
        earningComponent.setId(earningComponentId);
        earningComponent.setComponentName("Basic");
        earningComponent.setComponentType(PayrollComponentType.EARNING);

        PayrollComponentEntity deductionComponent = new PayrollComponentEntity();
        deductionComponent.setId(deductionComponentId);
        deductionComponent.setComponentName("PF");
        deductionComponent.setComponentType(PayrollComponentType.DEDUCTION);

        when(staffRepository.findByIdAndDeletedFalse(staffId)).thenReturn(Optional.of(staff));
        when(staffAttendanceRepository.findAllByStaffIdAndAttendanceDateBetweenAndDeletedFalse(
                any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(attendance(staffId, LocalDate.of(2026, 2, 10)), attendance(staffId, LocalDate.of(2026, 2, 11))));
        when(staffSalaryStructureRepository.findAllByStaffIdAndActiveTrueAndEffectiveFromLessThanEqualAndDeletedFalse(
                any(UUID.class), any(LocalDate.class))).thenReturn(List.of(earning, deduction));
        when(payrollComponentRepository.findAllByIdInAndDeletedFalse(any())).thenReturn(List.of(earningComponent, deductionComponent));
        when(payrollRepository.findByStaffIdAndYearAndMonthAndDeletedFalse(staffId, 2026, 2)).thenReturn(Optional.empty());
        when(payrollRepository.save(any(PayrollEntity.class))).thenAnswer(invocation -> {
            PayrollEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(payrollDetailRepository.findAllByPayrollIdAndDeletedFalse(any(UUID.class))).thenReturn(List.of());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        PayrollResponse response = payrollService.generate(
                new PayrollGenerateRequest(staffId, 2, 2026, 2, BigDecimal.valueOf(200), null, null),
                principal);

        assertEquals(BigDecimal.valueOf(30000).setScale(2), response.grossSalary());
        assertEquals(BigDecimal.valueOf(1700).setScale(2), response.totalDeductions());
        assertEquals(BigDecimal.valueOf(28300).setScale(2), response.netSalary());
        verify(payrollDetailRepository).saveAll(any());
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
    void bulkProcessShouldProcessMultiplePayrolls() {
        UUID payrollOneId = UUID.randomUUID();
        UUID payrollTwoId = UUID.randomUUID();

        PayrollEntity payrollOne = new PayrollEntity();
        payrollOne.setId(payrollOneId);
        payrollOne.setStatus(PayrollStatus.DRAFT);

        PayrollEntity payrollTwo = new PayrollEntity();
        payrollTwo.setId(payrollTwoId);
        payrollTwo.setStatus(PayrollStatus.DRAFT);

        when(payrollRepository.findByIdAndDeletedFalse(payrollOneId)).thenReturn(Optional.of(payrollOne));
        when(payrollRepository.findByIdAndDeletedFalse(payrollTwoId)).thenReturn(Optional.of(payrollTwo));
        when(payrollRepository.save(any(PayrollEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        List<PayrollResponse> response = payrollService.bulkProcess(
                new PayrollBulkProcessRequest(
                        List.of(payrollOneId, payrollTwoId),
                        "BANK_TRANSFER",
                        "BATCH-APR",
                        LocalDate.of(2026, 4, 30),
                        "https://files.example/payslips"),
                principal);

        assertEquals(2, response.size());
        assertEquals(PayrollStatus.PROCESSED, response.get(0).status());
        assertEquals("BATCH-APR-1", response.get(0).paymentReference());
        assertEquals("https://files.example/payslips/" + payrollOneId + ".pdf", response.get(0).payslipUrl());
    }

    @Test
    void getPayslipShouldIncludeManualDeductions() {
        UUID payrollId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        PayrollEntity payroll = new PayrollEntity();
        payroll.setId(payrollId);
        payroll.setStaffId(UUID.randomUUID());
        payroll.setMonth(2);
        payroll.setYear(2026);
        payroll.setGrossSalary(BigDecimal.valueOf(30000).setScale(2));
        payroll.setTotalDeductions(BigDecimal.valueOf(1500).setScale(2));
        payroll.setNetSalary(BigDecimal.valueOf(28500).setScale(2));
        payroll.setStatus(PayrollStatus.PROCESSED);
        payroll.setCreatedAt(Instant.parse("2026-02-28T10:00:00Z"));

        PayrollDetailEntity detail = new PayrollDetailEntity();
        detail.setId(UUID.randomUUID());
        detail.setPayrollId(payrollId);
        detail.setPayrollComponentId(componentId);
        detail.setAmount(BigDecimal.valueOf(1000).setScale(2));

        PayrollComponentEntity component = new PayrollComponentEntity();
        component.setId(componentId);
        component.setComponentName("PF");
        component.setComponentType(PayrollComponentType.DEDUCTION);
        component.setTaxable(false);

        when(payrollRepository.findByIdAndDeletedFalse(payrollId)).thenReturn(Optional.of(payroll));
        when(payrollDetailRepository.findAllByPayrollIdAndDeletedFalseOrderByCreatedAtAsc(payrollId))
                .thenReturn(List.of(detail));
        when(payrollComponentRepository.findAllByIdInAndDeletedFalse(any())).thenReturn(List.of(component));

        PayrollPayslipResponse response = payrollService.getPayslip(payrollId);

        assertEquals(1, response.deductions().size());
        assertEquals(BigDecimal.valueOf(500).setScale(2), response.manualDeductions());
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
