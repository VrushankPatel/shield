package com.shield.module.payroll.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.payroll.dto.PayrollBulkProcessRequest;
import com.shield.module.payroll.dto.PayrollDetailResponse;
import com.shield.module.payroll.dto.PayrollGenerateRequest;
import com.shield.module.payroll.dto.PayrollPayslipResponse;
import com.shield.module.payroll.dto.PayrollProcessRequest;
import com.shield.module.payroll.dto.PayrollResponse;
import com.shield.module.payroll.dto.PayrollSummaryResponse;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final StaffSalaryStructureRepository staffSalaryStructureRepository;
    private final PayrollComponentRepository payrollComponentRepository;
    private final PayrollDetailRepository payrollDetailRepository;
    private final AuditLogService auditLogService;

    public PayrollService(
            PayrollRepository payrollRepository,
            StaffRepository staffRepository,
            StaffAttendanceRepository staffAttendanceRepository,
            StaffSalaryStructureRepository staffSalaryStructureRepository,
            PayrollComponentRepository payrollComponentRepository,
            PayrollDetailRepository payrollDetailRepository,
            AuditLogService auditLogService) {
        this.payrollRepository = payrollRepository;
        this.staffRepository = staffRepository;
        this.staffAttendanceRepository = staffAttendanceRepository;
        this.staffSalaryStructureRepository = staffSalaryStructureRepository;
        this.payrollComponentRepository = payrollComponentRepository;
        this.payrollDetailRepository = payrollDetailRepository;
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

        List<StaffSalaryStructureEntity> salaryRows = staffSalaryStructureRepository
                .findAllByStaffIdAndActiveTrueAndEffectiveFromLessThanEqualAndDeletedFalse(staff.getId(), toDate);
        Map<UUID, PayrollComponentEntity> componentsById = loadComponents(salaryRows.stream()
                .map(StaffSalaryStructureEntity::getPayrollComponentId)
                .collect(Collectors.toSet()));

        List<PayrollDetailEntity> generatedDetails = new ArrayList<>();
        BigDecimal componentEarnings = ZERO;
        BigDecimal componentDeductions = ZERO;

        for (StaffSalaryStructureEntity row : salaryRows) {
            PayrollComponentEntity component = componentsById.get(row.getPayrollComponentId());
            if (component == null) {
                continue;
            }

            BigDecimal prorated = prorate(row.getAmount(), presentDays, request.workingDays());
            if (prorated.signum() <= 0) {
                continue;
            }

            if (component.getComponentType() == PayrollComponentType.DEDUCTION) {
                componentDeductions = componentDeductions.add(prorated);
            } else {
                componentEarnings = componentEarnings.add(prorated);
            }

            PayrollDetailEntity detail = new PayrollDetailEntity();
            detail.setTenantId(principal.tenantId());
            detail.setPayrollComponentId(row.getPayrollComponentId());
            detail.setAmount(prorated);
            generatedDetails.add(detail);
        }

        BigDecimal grossSalary = salaryRows.isEmpty()
                ? prorate(staff.getBasicSalary(), presentDays, request.workingDays())
                : componentEarnings;

        BigDecimal adHocDeductions = sanitizeMoney(request.totalDeductions());
        BigDecimal deductions = componentDeductions.add(adHocDeductions);
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
        replaceDetails(saved.getId(), generatedDetails);

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

    public PayrollResponse process(PayrollProcessRequest request, ShieldPrincipal principal) {
        PayrollEntity entity = payrollRepository.findByIdAndDeletedFalse(request.payrollId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + request.payrollId()));

        if (entity.getStatus() == PayrollStatus.PAID) {
            throw new BadRequestException("Paid payroll cannot be processed again");
        }

        entity.setPaymentMethod(request.paymentMethod());
        entity.setPaymentReference(request.paymentReference());
        entity.setPaymentDate(request.paymentDate() != null ? request.paymentDate() : LocalDate.now());
        entity.setPayslipUrl(request.payslipUrl());
        entity.setStatus(PayrollStatus.PROCESSED);

        PayrollEntity saved = payrollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "PAYROLL_PROCESSED", "payroll", saved.getId(), null);
        return toResponse(saved);
    }

    public List<PayrollResponse> bulkProcess(PayrollBulkProcessRequest request, ShieldPrincipal principal) {
        List<PayrollResponse> responses = new ArrayList<>();
        String referencePrefix = request.paymentReferencePrefix() == null || request.paymentReferencePrefix().isBlank()
                ? null
                : request.paymentReferencePrefix().trim();

        for (int i = 0; i < request.payrollIds().size(); i++) {
            UUID payrollId = request.payrollIds().get(i);
            PayrollEntity entity = payrollRepository.findByIdAndDeletedFalse(payrollId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + payrollId));

            if (entity.getStatus() == PayrollStatus.PAID) {
                throw new BadRequestException("Paid payroll cannot be processed again: " + payrollId);
            }

            String paymentReference = referencePrefix == null
                    ? (entity.getPaymentReference() == null || entity.getPaymentReference().isBlank()
                            ? "PAY-" + payrollId.toString().substring(0, 8).toUpperCase()
                            : entity.getPaymentReference())
                    : referencePrefix + "-" + (i + 1);

            entity.setPaymentMethod(request.paymentMethod());
            entity.setPaymentReference(paymentReference);
            entity.setPaymentDate(request.paymentDate() != null ? request.paymentDate() : LocalDate.now());
            entity.setPayslipUrl(buildPayslipUrl(request.payslipBaseUrl(), payrollId));
            entity.setStatus(PayrollStatus.PROCESSED);

            PayrollEntity saved = payrollRepository.save(entity);
            auditLogService.record(principal.tenantId(), principal.userId(), "PAYROLL_BULK_PROCESSED", "payroll", saved.getId(), null);
            responses.add(toResponse(saved));
        }
        return responses;
    }

    public PayrollResponse approve(UUID id, ShieldPrincipal principal) {
        PayrollEntity entity = payrollRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + id));

        if (entity.getStatus() != PayrollStatus.PROCESSED) {
            throw new BadRequestException("Only processed payroll can be approved");
        }

        entity.setStatus(PayrollStatus.PAID);
        if (entity.getPaymentDate() == null) {
            entity.setPaymentDate(LocalDate.now());
        }

        PayrollEntity saved = payrollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "PAYROLL_APPROVED", "payroll", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PayrollResponse> listByPeriod(int month, int year, Pageable pageable) {
        return PagedResponse.from(payrollRepository.findAllByYearAndMonthAndDeletedFalse(year, month, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PayrollResponse> listByStaff(UUID staffId, Pageable pageable) {
        return PagedResponse.from(payrollRepository.findAllByStaffIdAndDeletedFalse(staffId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PayrollPayslipResponse getPayslip(UUID payrollId) {
        PayrollEntity payroll = payrollRepository.findByIdAndDeletedFalse(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + payrollId));

        List<PayrollDetailEntity> details = payrollDetailRepository.findAllByPayrollIdAndDeletedFalseOrderByCreatedAtAsc(payrollId);
        Map<UUID, PayrollComponentEntity> componentsById = loadComponents(details.stream()
                .map(PayrollDetailEntity::getPayrollComponentId)
                .collect(Collectors.toSet()));

        List<PayrollDetailResponse> earnings = new ArrayList<>();
        List<PayrollDetailResponse> deductions = new ArrayList<>();
        BigDecimal componentDeductionsTotal = ZERO;

        for (PayrollDetailEntity detail : details) {
            PayrollComponentEntity component = componentsById.get(detail.getPayrollComponentId());
            PayrollComponentType type = component == null ? PayrollComponentType.EARNING : component.getComponentType();
            String componentName = component == null
                    ? "UNKNOWN_COMPONENT_" + detail.getPayrollComponentId()
                    : component.getComponentName();
            boolean taxable = component != null && component.isTaxable();

            PayrollDetailResponse detailResponse = new PayrollDetailResponse(
                    detail.getId(),
                    detail.getPayrollComponentId(),
                    componentName,
                    type,
                    detail.getAmount(),
                    taxable);

            if (type == PayrollComponentType.DEDUCTION) {
                deductions.add(detailResponse);
                componentDeductionsTotal = componentDeductionsTotal.add(detail.getAmount());
            } else {
                earnings.add(detailResponse);
            }
        }

        BigDecimal manualDeductions = payroll.getTotalDeductions().subtract(componentDeductionsTotal).setScale(2, RoundingMode.HALF_UP);
        if (manualDeductions.signum() < 0) {
            manualDeductions = ZERO;
        }

        return new PayrollPayslipResponse(
                payroll.getId(),
                payroll.getStaffId(),
                payroll.getMonth(),
                payroll.getYear(),
                payroll.getGrossSalary(),
                payroll.getTotalDeductions(),
                manualDeductions,
                payroll.getNetSalary(),
                payroll.getPaymentDate(),
                payroll.getStatus(),
                payroll.getPayslipUrl(),
                payroll.getCreatedAt(),
                List.copyOf(earnings),
                List.copyOf(deductions));
    }

    @Transactional(readOnly = true)
    public PayrollSummaryResponse summarize(Integer month, Integer year) {
        List<Object[]> rows = payrollRepository.summarize(year, month);
        Object[] row = rows.isEmpty() ? new Object[] {0L, ZERO, ZERO, ZERO} : rows.get(0);

        return new PayrollSummaryResponse(
                year,
                month,
                toLong(row[0]),
                toMoney(row[1]),
                toMoney(row[2]),
                toMoney(row[3]));
    }

    private Map<UUID, PayrollComponentEntity> loadComponents(Set<UUID> componentIds) {
        if (componentIds.isEmpty()) {
            return Map.of();
        }
        return payrollComponentRepository.findAllByIdInAndDeletedFalse(componentIds).stream()
                .collect(Collectors.toMap(PayrollComponentEntity::getId, Function.identity()));
    }

    private void replaceDetails(UUID payrollId, List<PayrollDetailEntity> freshDetails) {
        List<PayrollDetailEntity> existingDetails = payrollDetailRepository.findAllByPayrollIdAndDeletedFalse(payrollId);
        if (!existingDetails.isEmpty()) {
            for (PayrollDetailEntity existingDetail : existingDetails) {
                existingDetail.setDeleted(true);
            }
            payrollDetailRepository.saveAll(existingDetails);
        }

        if (!freshDetails.isEmpty()) {
            for (PayrollDetailEntity detail : freshDetails) {
                detail.setPayrollId(payrollId);
            }
            payrollDetailRepository.saveAll(freshDetails);
        }
    }

    private String buildPayslipUrl(String payslipBaseUrl, UUID payrollId) {
        if (payslipBaseUrl == null || payslipBaseUrl.isBlank()) {
            return null;
        }
        String base = payslipBaseUrl.trim();
        String normalized = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return normalized + "/" + payrollId + ".pdf";
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

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toMoney(Object value) {
        if (value == null) {
            return ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
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
