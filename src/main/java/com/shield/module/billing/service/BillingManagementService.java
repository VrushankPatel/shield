package com.shield.module.billing.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.billing.dto.BillingCycleCreateRequest;
import com.shield.module.billing.dto.BillingCycleResponse;
import com.shield.module.billing.dto.BillingCycleUpdateRequest;
import com.shield.module.billing.dto.InvoiceBulkGenerateRequest;
import com.shield.module.billing.dto.InvoiceGenerateRequest;
import com.shield.module.billing.dto.InvoiceResponse;
import com.shield.module.billing.dto.InvoiceUpdateRequest;
import com.shield.module.billing.dto.LateFeeRuleCreateRequest;
import com.shield.module.billing.dto.LateFeeRuleResponse;
import com.shield.module.billing.dto.LateFeeRuleUpdateRequest;
import com.shield.module.billing.dto.MaintenanceChargeGenerateRequest;
import com.shield.module.billing.dto.MaintenanceChargeResponse;
import com.shield.module.billing.dto.MaintenanceChargeUpdateRequest;
import com.shield.module.billing.dto.PaymentReminderResponse;
import com.shield.module.billing.dto.PaymentReminderScheduleRequest;
import com.shield.module.billing.dto.PaymentReminderSendRequest;
import com.shield.module.billing.dto.SpecialAssessmentCreateRequest;
import com.shield.module.billing.dto.SpecialAssessmentResponse;
import com.shield.module.billing.dto.SpecialAssessmentUpdateRequest;
import com.shield.module.billing.entity.BillingCycleEntity;
import com.shield.module.billing.entity.BillingCycleStatus;
import com.shield.module.billing.entity.InvoiceEntity;
import com.shield.module.billing.entity.InvoiceStatus;
import com.shield.module.billing.entity.LateFeeRuleEntity;
import com.shield.module.billing.entity.LateFeeType;
import com.shield.module.billing.entity.MaintenanceChargeEntity;
import com.shield.module.billing.entity.PaymentReminderEntity;
import com.shield.module.billing.entity.ReminderStatus;
import com.shield.module.billing.entity.SpecialAssessmentEntity;
import com.shield.module.billing.entity.SpecialAssessmentStatus;
import com.shield.module.billing.repository.BillingCycleRepository;
import com.shield.module.billing.repository.InvoiceRepository;
import com.shield.module.billing.repository.LateFeeRuleRepository;
import com.shield.module.billing.repository.MaintenanceChargeRepository;
import com.shield.module.billing.repository.PaymentReminderRepository;
import com.shield.module.billing.repository.SpecialAssessmentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingManagementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String ENTITY_BILLING_CYCLE = "billing_cycle";
    private static final String ENTITY_MAINTENANCE_CHARGE = "maintenance_charge";
    private static final String ENTITY_SPECIAL_ASSESSMENT = "special_assessment";
    private static final String ENTITY_INVOICE = "invoice";
    private static final String ENTITY_LATE_FEE_RULE = "late_fee_rule";

    private final BillingCycleRepository billingCycleRepository;
    private final MaintenanceChargeRepository maintenanceChargeRepository;
    private final SpecialAssessmentRepository specialAssessmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentReminderRepository paymentReminderRepository;
    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final AuditLogService auditLogService;

    public BillingCycleResponse createBillingCycle(BillingCycleCreateRequest request, ShieldPrincipal principal) {
        BillingCycleEntity entity = new BillingCycleEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCycleName(request.cycleName().trim());
        entity.setMonth(request.month());
        entity.setYear(request.year());
        entity.setDueDate(request.dueDate());
        entity.setLateFeeApplicableDate(request.lateFeeApplicableDate());
        entity.setStatus(BillingCycleStatus.DRAFT);
        BillingCycleEntity saved = billingCycleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BILLING_CYCLE_CREATED", ENTITY_BILLING_CYCLE, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BillingCycleResponse getBillingCycle(UUID id) {
        return toResponse(findBillingCycle(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BillingCycleResponse> listBillingCycles(Pageable pageable) {
        return PagedResponse.from(billingCycleRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BillingCycleResponse> listBillingCyclesByYear(int year, Pageable pageable) {
        return PagedResponse.from(billingCycleRepository.findAllByYearAndDeletedFalse(year, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BillingCycleResponse getCurrentBillingCycle() {
        BillingCycleEntity cycle = billingCycleRepository.findFirstByStatusAndDeletedFalseOrderByYearDescMonthDesc(BillingCycleStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("No published billing cycle found"));
        return toResponse(cycle);
    }

    public BillingCycleResponse updateBillingCycle(UUID id, BillingCycleUpdateRequest request, ShieldPrincipal principal) {
        BillingCycleEntity entity = findBillingCycle(id);
        if (entity.getStatus() == BillingCycleStatus.CLOSED) {
            throw new BadRequestException("Closed billing cycle cannot be updated");
        }
        entity.setCycleName(request.cycleName().trim());
        entity.setMonth(request.month());
        entity.setYear(request.year());
        entity.setDueDate(request.dueDate());
        entity.setLateFeeApplicableDate(request.lateFeeApplicableDate());
        BillingCycleEntity saved = billingCycleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BILLING_CYCLE_UPDATED", ENTITY_BILLING_CYCLE, saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteBillingCycle(UUID id, ShieldPrincipal principal) {
        BillingCycleEntity entity = findBillingCycle(id);
        entity.setDeleted(true);
        billingCycleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BILLING_CYCLE_DELETED", ENTITY_BILLING_CYCLE, id, null);
    }

    public BillingCycleResponse publishBillingCycle(UUID id, ShieldPrincipal principal) {
        BillingCycleEntity entity = findBillingCycle(id);
        if (entity.getStatus() != BillingCycleStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT cycle can be published");
        }
        entity.setStatus(BillingCycleStatus.PUBLISHED);
        BillingCycleEntity saved = billingCycleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BILLING_CYCLE_PUBLISHED", ENTITY_BILLING_CYCLE, id, null);
        return toResponse(saved);
    }

    public BillingCycleResponse closeBillingCycle(UUID id, ShieldPrincipal principal) {
        BillingCycleEntity entity = findBillingCycle(id);
        if (entity.getStatus() != BillingCycleStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED cycle can be closed");
        }
        entity.setStatus(BillingCycleStatus.CLOSED);
        BillingCycleEntity saved = billingCycleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BILLING_CYCLE_CLOSED", ENTITY_BILLING_CYCLE, id, null);
        return toResponse(saved);
    }

    public MaintenanceChargeResponse generateMaintenanceCharge(MaintenanceChargeGenerateRequest request, ShieldPrincipal principal) {
        MaintenanceChargeEntity entity = new MaintenanceChargeEntity();
        entity.setTenantId(principal.tenantId());
        entity.setUnitId(request.unitId());
        entity.setBillingCycleId(request.billingCycleId());
        entity.setBaseAmount(request.baseAmount());
        entity.setCalculationMethod(trimToNull(request.calculationMethod()));
        entity.setAreaBasedAmount(request.areaBasedAmount());
        entity.setFixedAmount(request.fixedAmount());
        entity.setTotalAmount(request.totalAmount());
        MaintenanceChargeEntity saved = maintenanceChargeRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MAINTENANCE_CHARGE_CREATED", ENTITY_MAINTENANCE_CHARGE, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MaintenanceChargeResponse getMaintenanceCharge(UUID id) {
        return toResponse(findMaintenanceCharge(id));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceChargeResponse> listMaintenanceCharges() {
        return maintenanceChargeRepository.findAllByDeletedFalse().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceChargeResponse> listMaintenanceChargesByCycle(UUID cycleId) {
        return maintenanceChargeRepository.findAllByBillingCycleIdAndDeletedFalse(cycleId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceChargeResponse> listMaintenanceChargesByUnit(UUID unitId) {
        return maintenanceChargeRepository.findAllByUnitIdAndDeletedFalse(unitId).stream().map(this::toResponse).toList();
    }

    public MaintenanceChargeResponse updateMaintenanceCharge(UUID id, MaintenanceChargeUpdateRequest request, ShieldPrincipal principal) {
        MaintenanceChargeEntity entity = findMaintenanceCharge(id);
        entity.setBaseAmount(request.baseAmount());
        entity.setCalculationMethod(trimToNull(request.calculationMethod()));
        entity.setAreaBasedAmount(request.areaBasedAmount());
        entity.setFixedAmount(request.fixedAmount());
        entity.setTotalAmount(request.totalAmount());
        MaintenanceChargeEntity saved = maintenanceChargeRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MAINTENANCE_CHARGE_UPDATED", ENTITY_MAINTENANCE_CHARGE, saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteMaintenanceCharge(UUID id, ShieldPrincipal principal) {
        MaintenanceChargeEntity entity = findMaintenanceCharge(id);
        entity.setDeleted(true);
        maintenanceChargeRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MAINTENANCE_CHARGE_DELETED", ENTITY_MAINTENANCE_CHARGE, id, null);
    }

    public SpecialAssessmentResponse createSpecialAssessment(SpecialAssessmentCreateRequest request, ShieldPrincipal principal) {
        SpecialAssessmentEntity entity = new SpecialAssessmentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setAssessmentName(request.assessmentName().trim());
        entity.setDescription(trimToNull(request.description()));
        entity.setTotalAmount(request.totalAmount());
        entity.setPerUnitAmount(request.perUnitAmount());
        entity.setAssessmentDate(request.assessmentDate());
        entity.setDueDate(request.dueDate());
        entity.setCreatedBy(principal.userId());
        entity.setStatus(SpecialAssessmentStatus.ACTIVE);
        SpecialAssessmentEntity saved = specialAssessmentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "SPECIAL_ASSESSMENT_CREATED", ENTITY_SPECIAL_ASSESSMENT, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SpecialAssessmentResponse getSpecialAssessment(UUID id) {
        return toResponse(findSpecialAssessment(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SpecialAssessmentResponse> listSpecialAssessments(Pageable pageable) {
        return PagedResponse.from(specialAssessmentRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SpecialAssessmentResponse> listActiveSpecialAssessments(Pageable pageable) {
        return PagedResponse.from(specialAssessmentRepository
                .findAllByStatusAndDueDateGreaterThanEqualAndDeletedFalse(SpecialAssessmentStatus.ACTIVE, LocalDate.now(), pageable)
                .map(this::toResponse));
    }

    public SpecialAssessmentResponse updateSpecialAssessment(UUID id, SpecialAssessmentUpdateRequest request, ShieldPrincipal principal) {
        SpecialAssessmentEntity entity = findSpecialAssessment(id);
        entity.setAssessmentName(request.assessmentName().trim());
        entity.setDescription(trimToNull(request.description()));
        entity.setTotalAmount(request.totalAmount());
        entity.setPerUnitAmount(request.perUnitAmount());
        entity.setAssessmentDate(request.assessmentDate());
        entity.setDueDate(request.dueDate());
        entity.setStatus(parseSpecialAssessmentStatus(request.status()));
        SpecialAssessmentEntity saved = specialAssessmentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "SPECIAL_ASSESSMENT_UPDATED", ENTITY_SPECIAL_ASSESSMENT, saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteSpecialAssessment(UUID id, ShieldPrincipal principal) {
        SpecialAssessmentEntity entity = findSpecialAssessment(id);
        entity.setDeleted(true);
        specialAssessmentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "SPECIAL_ASSESSMENT_DELETED", ENTITY_SPECIAL_ASSESSMENT, id, null);
    }

    public InvoiceResponse generateInvoice(InvoiceGenerateRequest request, ShieldPrincipal principal) {
        InvoiceEntity saved = invoiceRepository.save(buildInvoiceEntity(request, principal.tenantId()));
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "INVOICE_GENERATED", ENTITY_INVOICE, saved.getId(), null);
        return toResponse(saved);
    }

    public List<InvoiceResponse> bulkGenerateInvoices(InvoiceBulkGenerateRequest request, ShieldPrincipal principal) {
        List<InvoiceEntity> entities = request.unitIds().stream()
                .map(unitId -> {
                    InvoiceGenerateRequest invoiceRequest = new InvoiceGenerateRequest(
                            unitId,
                            request.billingCycleId(),
                            null,
                            request.dueDate(),
                            request.subtotal(),
                            request.lateFee(),
                            request.gstAmount(),
                            request.otherCharges());
                    return buildInvoiceEntity(invoiceRequest, principal.tenantId());
                })
                .toList();
        List<InvoiceEntity> saved = invoiceRepository.saveAll(entities);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "INVOICE_BULK_GENERATED", ENTITY_INVOICE, null, null);
        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        return toResponse(findInvoice(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> listInvoices(Pageable pageable) {
        return PagedResponse.from(invoiceRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoicesByUnit(UUID unitId) {
        return invoiceRepository.findAllByUnitIdAndDeletedFalse(unitId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoicesByCycle(UUID cycleId) {
        return invoiceRepository.findAllByBillingCycleIdAndDeletedFalse(cycleId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> listInvoicesByStatus(String status, Pageable pageable) {
        InvoiceStatus parsed = parseInvoiceStatus(status);
        return PagedResponse.from(invoiceRepository.findAllByStatusAndDeletedFalse(parsed, pageable).map(this::toResponse));
    }

    public InvoiceResponse updateInvoice(UUID id, InvoiceUpdateRequest request, ShieldPrincipal principal) {
        InvoiceEntity entity = findInvoice(id);
        entity.setDueDate(request.dueDate());
        entity.setSubtotal(request.subtotal());
        entity.setLateFee(request.lateFee());
        entity.setGstAmount(request.gstAmount());
        entity.setOtherCharges(request.otherCharges());
        entity.setTotalAmount(request.subtotal().add(request.lateFee()).add(request.gstAmount()).add(request.otherCharges()));
        entity.setOutstandingAmount(request.outstandingAmount());
        entity.setStatus(parseInvoiceStatus(request.status()));
        InvoiceEntity saved = invoiceRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "INVOICE_UPDATED", ENTITY_INVOICE, saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteInvoice(UUID id, ShieldPrincipal principal) {
        InvoiceEntity entity = findInvoice(id);
        entity.setDeleted(true);
        invoiceRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "INVOICE_DELETED", ENTITY_INVOICE, id, null);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceDownloadInfo(UUID id) {
        return toResponse(findInvoice(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> listDefaulters(Pageable pageable) {
        return PagedResponse.from(invoiceRepository
                .findAllByDueDateBeforeAndOutstandingAmountGreaterThanAndDeletedFalse(LocalDate.now(), ZERO, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> listOutstanding(Pageable pageable) {
        return PagedResponse.from(invoiceRepository.findAllByOutstandingAmountGreaterThanAndDeletedFalse(ZERO, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentReminderResponse> listPaymentReminders(Pageable pageable) {
        return PagedResponse.from(paymentReminderRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public PaymentReminderResponse sendPaymentReminder(PaymentReminderSendRequest request, ShieldPrincipal principal) {
        findInvoice(request.invoiceId());
        PaymentReminderEntity entity = new PaymentReminderEntity();
        entity.setTenantId(principal.tenantId());
        entity.setInvoiceId(request.invoiceId());
        entity.setReminderType(request.reminderType().trim().toUpperCase(Locale.ROOT));
        entity.setChannel(request.channel().trim().toUpperCase(Locale.ROOT));
        entity.setSentAt(Instant.now());
        entity.setStatus(ReminderStatus.SENT);
        PaymentReminderEntity saved = paymentReminderRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYMENT_REMINDER_SENT", "payment_reminder", saved.getId(), null);
        return toResponse(saved);
    }

    public PaymentReminderResponse schedulePaymentReminder(PaymentReminderScheduleRequest request, ShieldPrincipal principal) {
        findInvoice(request.invoiceId());
        PaymentReminderEntity entity = new PaymentReminderEntity();
        entity.setTenantId(principal.tenantId());
        entity.setInvoiceId(request.invoiceId());
        entity.setReminderType(request.reminderType().trim().toUpperCase(Locale.ROOT));
        entity.setChannel(request.channel().trim().toUpperCase(Locale.ROOT));
        entity.setSentAt(request.scheduledAt());
        entity.setStatus(ReminderStatus.SCHEDULED);
        PaymentReminderEntity saved = paymentReminderRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYMENT_REMINDER_SCHEDULED", "payment_reminder", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentReminderResponse> listPaymentRemindersByInvoice(UUID invoiceId) {
        return paymentReminderRepository.findAllByInvoiceIdAndDeletedFalse(invoiceId).stream().map(this::toResponse).toList();
    }

    public LateFeeRuleResponse createLateFeeRule(LateFeeRuleCreateRequest request, ShieldPrincipal principal) {
        LateFeeRuleEntity entity = new LateFeeRuleEntity();
        entity.setTenantId(principal.tenantId());
        entity.setRuleName(request.ruleName().trim());
        entity.setDaysAfterDue(request.daysAfterDue());
        entity.setFeeType(parseLateFeeType(request.feeType()));
        entity.setFeeAmount(request.feeAmount());
        entity.setActive(true);
        LateFeeRuleEntity saved = lateFeeRuleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "LATE_FEE_RULE_CREATED", ENTITY_LATE_FEE_RULE, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LateFeeRuleResponse getLateFeeRule(UUID id) {
        return toResponse(findLateFeeRule(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LateFeeRuleResponse> listLateFeeRules(Pageable pageable) {
        return PagedResponse.from(lateFeeRuleRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public LateFeeRuleResponse updateLateFeeRule(UUID id, LateFeeRuleUpdateRequest request, ShieldPrincipal principal) {
        LateFeeRuleEntity entity = findLateFeeRule(id);
        entity.setRuleName(request.ruleName().trim());
        entity.setDaysAfterDue(request.daysAfterDue());
        entity.setFeeType(parseLateFeeType(request.feeType()));
        entity.setFeeAmount(request.feeAmount());
        entity.setActive(request.active());
        LateFeeRuleEntity saved = lateFeeRuleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "LATE_FEE_RULE_UPDATED", ENTITY_LATE_FEE_RULE, saved.getId(), null);
        return toResponse(saved);
    }

    public void deleteLateFeeRule(UUID id, ShieldPrincipal principal) {
        LateFeeRuleEntity entity = findLateFeeRule(id);
        entity.setDeleted(true);
        lateFeeRuleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "LATE_FEE_RULE_DELETED", ENTITY_LATE_FEE_RULE, id, null);
    }

    public LateFeeRuleResponse activateLateFeeRule(UUID id, ShieldPrincipal principal) {
        LateFeeRuleEntity entity = findLateFeeRule(id);
        entity.setActive(true);
        LateFeeRuleEntity saved = lateFeeRuleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "LATE_FEE_RULE_ACTIVATED", ENTITY_LATE_FEE_RULE, id, null);
        return toResponse(saved);
    }

    public LateFeeRuleResponse deactivateLateFeeRule(UUID id, ShieldPrincipal principal) {
        LateFeeRuleEntity entity = findLateFeeRule(id);
        entity.setActive(false);
        LateFeeRuleEntity saved = lateFeeRuleRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "LATE_FEE_RULE_DEACTIVATED", ENTITY_LATE_FEE_RULE, id, null);
        return toResponse(saved);
    }

    private BillingCycleEntity findBillingCycle(UUID id) {
        return billingCycleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found: " + id));
    }

    private MaintenanceChargeEntity findMaintenanceCharge(UUID id) {
        return maintenanceChargeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance charge not found: " + id));
    }

    private SpecialAssessmentEntity findSpecialAssessment(UUID id) {
        return specialAssessmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Special assessment not found: " + id));
    }

    private InvoiceEntity findInvoice(UUID id) {
        return invoiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    private LateFeeRuleEntity findLateFeeRule(UUID id) {
        return lateFeeRuleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Late fee rule not found: " + id));
    }

    private BillingCycleResponse toResponse(BillingCycleEntity entity) {
        return new BillingCycleResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCycleName(),
                entity.getMonth(),
                entity.getYear(),
                entity.getDueDate(),
                entity.getLateFeeApplicableDate(),
                entity.getStatus());
    }

    private MaintenanceChargeResponse toResponse(MaintenanceChargeEntity entity) {
        return new MaintenanceChargeResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUnitId(),
                entity.getBillingCycleId(),
                entity.getBaseAmount(),
                entity.getCalculationMethod(),
                entity.getAreaBasedAmount(),
                entity.getFixedAmount(),
                entity.getTotalAmount());
    }

    private SpecialAssessmentResponse toResponse(SpecialAssessmentEntity entity) {
        return new SpecialAssessmentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAssessmentName(),
                entity.getDescription(),
                entity.getTotalAmount(),
                entity.getPerUnitAmount(),
                entity.getAssessmentDate(),
                entity.getDueDate(),
                entity.getCreatedBy(),
                entity.getStatus());
    }

    private InvoiceResponse toResponse(InvoiceEntity entity) {
        return new InvoiceResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getInvoiceNumber(),
                entity.getUnitId(),
                entity.getBillingCycleId(),
                entity.getInvoiceDate(),
                entity.getDueDate(),
                entity.getSubtotal(),
                entity.getLateFee(),
                entity.getGstAmount(),
                entity.getOtherCharges(),
                entity.getTotalAmount(),
                entity.getOutstandingAmount(),
                entity.getStatus());
    }

    private PaymentReminderResponse toResponse(PaymentReminderEntity entity) {
        return new PaymentReminderResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getInvoiceId(),
                entity.getReminderType(),
                entity.getSentAt(),
                entity.getChannel(),
                entity.getStatus());
    }

    private LateFeeRuleResponse toResponse(LateFeeRuleEntity entity) {
        return new LateFeeRuleResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getRuleName(),
                entity.getDaysAfterDue(),
                entity.getFeeType(),
                entity.getFeeAmount(),
                entity.isActive());
    }

    private InvoiceEntity buildInvoiceEntity(InvoiceGenerateRequest request, UUID tenantId) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setTenantId(tenantId);
        entity.setInvoiceNumber(generateInvoiceNumber());
        entity.setUnitId(request.unitId());
        entity.setBillingCycleId(request.billingCycleId());
        entity.setInvoiceDate(request.invoiceDate() != null ? request.invoiceDate() : LocalDate.now());
        entity.setDueDate(request.dueDate());
        entity.setSubtotal(request.subtotal());
        entity.setLateFee(request.lateFee());
        entity.setGstAmount(request.gstAmount());
        entity.setOtherCharges(request.otherCharges());
        BigDecimal total = request.subtotal().add(request.lateFee()).add(request.gstAmount()).add(request.otherCharges());
        entity.setTotalAmount(total);
        entity.setOutstandingAmount(total);
        entity.setStatus(InvoiceStatus.UNPAID);
        return entity;
    }

    private String generateInvoiceNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String number = "INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
            if (invoiceRepository.findByInvoiceNumberAndDeletedFalse(number).isEmpty()) {
                return number;
            }
        }
        throw new BadRequestException("Unable to generate unique invoice number");
    }

    private InvoiceStatus parseInvoiceStatus(String value) {
        try {
            return InvoiceStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid invoice status: " + value);
        }
    }

    private SpecialAssessmentStatus parseSpecialAssessmentStatus(String value) {
        try {
            return SpecialAssessmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid special assessment status: " + value);
        }
    }

    private LateFeeType parseLateFeeType(String value) {
        try {
            return LateFeeType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid late fee type: " + value);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
