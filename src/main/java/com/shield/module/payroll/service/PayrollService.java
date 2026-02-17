package com.shield.module.payroll.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.payroll.dto.PayrollGenerateRequest;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayrollService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PayrollRepository payrollRepository;
    private final StaffRepository staffRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final AuditLogService auditLogService;

    public PayrollService(
            PayrollRepository payrollRepository,
            StaffRepository staffRepository,
            StaffAttendanceRepository staffAttendanceRepository,
            AuditLogService auditLogService) {
        this.payrollRepository = payrollRepository;
        this.staffRepository = staffRepository;
        this.staffAttendanceRepository = staffAttendanceRepository;
        this.auditLogService = auditLogService;
    }

    public PayrollResponse generate(PayrollGenerateRequest request, ShieldPrincipal principal) {
        StaffEntity staff = staffRepository.findByIdAndDeletedFalse(request.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + request.staffId()));

        YearMonth yearMonth = YearMonth.of(request.year(), request.month());
        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        List<StaffAttendanceEntity> attendances = staffAttendanceRepository
                .findAllByStaffIdAndAttendanceDateBetweenAndDeletedFalse(staff.getId(), fromDate, toDate);

        int presentDays = (int) attendances.stream()
                .filter(attendance -> attendance.getStatus() != StaffAttendanceStatus.ABSENT)
                .filter(attendance -> attendance.getCheckInTime() != null)
                .count();

        BigDecimal grossSalary = prorate(staff.getBasicSalary(), presentDays, request.workingDays());
        BigDecimal deductions = sanitizeMoney(request.totalDeductions());
        BigDecimal netSalary = grossSalary.subtract(deductions);
        if (netSalary.signum() < 0) {
            netSalary = ZERO;
        }

        PayrollEntity entity = payrollRepository.findByStaffIdAndYearAndMonthAndDeletedFalse(
                        request.staffId(), request.year(), request.month())
                .orElseGet(() -> {
                    PayrollEntity payroll = new PayrollEntity();
                    payroll.setTenantId(principal.tenantId());
                    payroll.setStaffId(request.staffId());
                    payroll.setMonth(request.month());
                    payroll.setYear(request.year());
                    return payroll;
                });

        entity.setWorkingDays(request.workingDays());
        entity.setPresentDays(presentDays);
        entity.setGrossSalary(grossSalary);
        entity.setTotalDeductions(deductions);
        entity.setNetSalary(netSalary);
        entity.setPaymentMethod(request.paymentMethod());
        entity.setPaymentReference(request.paymentReference());
        entity.setStatus(request.paymentMethod() == null || request.paymentMethod().isBlank()
                ? PayrollStatus.DRAFT
                : PayrollStatus.PROCESSED);
        entity.setPaymentDate(entity.getStatus() == PayrollStatus.PROCESSED ? LocalDate.now() : null);

        PayrollEntity saved = payrollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "PAYROLL_GENERATED", "payroll", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PayrollResponse> list(Pageable pageable) {
        return PagedResponse.from(payrollRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PayrollResponse getById(UUID id) {
        PayrollEntity entity = payrollRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + id));
        return toResponse(entity);
    }

    private BigDecimal sanitizeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal prorate(BigDecimal monthlySalary, int presentDays, int workingDays) {
        if (workingDays <= 0 || monthlySalary == null || monthlySalary.signum() <= 0) {
            return ZERO;
        }
        BigDecimal perDay = monthlySalary.divide(BigDecimal.valueOf(workingDays), 8, RoundingMode.HALF_UP);
        return perDay.multiply(BigDecimal.valueOf(Math.max(0, presentDays))).setScale(2, RoundingMode.HALF_UP);
    }

    private PayrollResponse toResponse(PayrollEntity entity) {
        return new PayrollResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getStaffId(),
                entity.getMonth(),
                entity.getYear(),
                entity.getWorkingDays(),
                entity.getPresentDays(),
                entity.getGrossSalary(),
                entity.getTotalDeductions(),
                entity.getNetSalary(),
                entity.getPaymentDate(),
                entity.getPaymentMethod(),
                entity.getPaymentReference(),
                entity.getStatus(),
                entity.getPayslipUrl());
    }
}
