package com.shield.module.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.billing.dto.BillingCycleCreateRequest;
import com.shield.module.billing.dto.BillingCycleUpdateRequest;
import com.shield.module.billing.dto.InvoiceBulkGenerateRequest;
import com.shield.module.billing.dto.InvoiceGenerateRequest;
import com.shield.module.billing.dto.InvoiceUpdateRequest;
import com.shield.module.billing.dto.LateFeeRuleCreateRequest;
import com.shield.module.billing.dto.LateFeeRuleUpdateRequest;
import com.shield.module.billing.dto.MaintenanceChargeGenerateRequest;
import com.shield.module.billing.dto.MaintenanceChargeUpdateRequest;
import com.shield.module.billing.dto.PaymentReminderResponse;
import com.shield.module.billing.dto.PaymentReminderScheduleRequest;
import com.shield.module.billing.dto.PaymentReminderSendRequest;
import com.shield.module.billing.dto.SpecialAssessmentCreateRequest;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class BillingManagementServiceTest {

    @Mock
    private BillingCycleRepository billingCycleRepository;
    @Mock
    private MaintenanceChargeRepository maintenanceChargeRepository;
    @Mock
    private SpecialAssessmentRepository specialAssessmentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentReminderRepository paymentReminderRepository;
    @Mock
    private LateFeeRuleRepository lateFeeRuleRepository;
    @Mock
    private AuditLogService auditLogService;

    private BillingManagementService billingManagementService;

    @BeforeEach
    void setUp() {
        billingManagementService = new BillingManagementService(
                billingCycleRepository,
                maintenanceChargeRepository,
                specialAssessmentRepository,
                invoiceRepository,
                paymentReminderRepository,
                lateFeeRuleRepository,
                auditLogService);
    }

    @Test
    void createBillingCycleShouldSetDraftStatus() {
        ShieldPrincipal principal = principal();
        BillingCycleCreateRequest request = new BillingCycleCreateRequest(
                "March 2026",
                3,
                2026,
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 4, 5));

        when(billingCycleRepository.save(any(BillingCycleEntity.class))).thenAnswer(invocation -> {
            BillingCycleEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = billingManagementService.createBillingCycle(request, principal);

        assertNotNull(response.id());
        assertEquals(BillingCycleStatus.DRAFT, response.status());
        assertEquals("March 2026", response.cycleName());
    }

    @Test
    void listBillingCyclesShouldReturnPagedResponse() {
        BillingCycleEntity cycle = billingCycle(BillingCycleStatus.DRAFT);
        cycle.setId(UUID.randomUUID());

        when(billingCycleRepository.findAllByDeletedFalse(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(cycle), PageRequest.of(0, 10), 1));

        var response = billingManagementService.listBillingCycles(PageRequest.of(0, 10));

        assertEquals(1, response.content().size());
        assertEquals(cycle.getId(), response.content().get(0).id());
    }

    @Test
    void listBillingCyclesByYearShouldReturnPagedResponse() {
        BillingCycleEntity cycle = billingCycle(BillingCycleStatus.PUBLISHED);
        cycle.setId(UUID.randomUUID());

        when(billingCycleRepository.findAllByYearAndDeletedFalse(2026, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(cycle), PageRequest.of(0, 5), 1));

        var response = billingManagementService.listBillingCyclesByYear(2026, PageRequest.of(0, 5));

        assertEquals(1, response.content().size());
        assertEquals(2026, response.content().get(0).year());
    }

    @Test
    void publishBillingCycleShouldTransitionFromDraftToPublished() {
        ShieldPrincipal principal = principal();
        UUID cycleId = UUID.randomUUID();
        BillingCycleEntity entity = billingCycle(BillingCycleStatus.DRAFT);
        entity.setId(cycleId);

        when(billingCycleRepository.findByIdAndDeletedFalse(cycleId)).thenReturn(Optional.of(entity));
        when(billingCycleRepository.save(any(BillingCycleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = billingManagementService.publishBillingCycle(cycleId, principal);

        assertEquals(BillingCycleStatus.PUBLISHED, response.status());
    }

    @Test
    void closeBillingCycleShouldTransitionWhenPublished() {
        ShieldPrincipal principal = principal();
        UUID cycleId = UUID.randomUUID();
        BillingCycleEntity entity = billingCycle(BillingCycleStatus.PUBLISHED);
        entity.setId(cycleId);

        when(billingCycleRepository.findByIdAndDeletedFalse(cycleId)).thenReturn(Optional.of(entity));
        when(billingCycleRepository.save(any(BillingCycleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = billingManagementService.closeBillingCycle(cycleId, principal);

        assertEquals(BillingCycleStatus.CLOSED, response.status());
    }

    @Test
    void closeBillingCycleShouldRejectWhenNotPublished() {
        ShieldPrincipal principal = principal();
        UUID cycleId = UUID.randomUUID();
        BillingCycleEntity entity = billingCycle(BillingCycleStatus.DRAFT);
        entity.setId(cycleId);

        when(billingCycleRepository.findByIdAndDeletedFalse(cycleId)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> billingManagementService.closeBillingCycle(cycleId, principal));
    }

    @Test
    void updateBillingCycleShouldRejectClosedCycle() {
        ShieldPrincipal principal = principal();
        UUID cycleId = UUID.randomUUID();
        BillingCycleEntity entity = billingCycle(BillingCycleStatus.CLOSED);
        entity.setId(cycleId);

        BillingCycleUpdateRequest request = new BillingCycleUpdateRequest(
                "Updated",
                4,
                2026,
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 5, 5));

        when(billingCycleRepository.findByIdAndDeletedFalse(cycleId)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> billingManagementService.updateBillingCycle(cycleId, request, principal));
    }

    @Test
    void getCurrentBillingCycleShouldThrowWhenNoPublishedCycleExists() {
        when(billingCycleRepository.findFirstByStatusAndDeletedFalseOrderByYearDescMonthDesc(BillingCycleStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billingManagementService.getCurrentBillingCycle());
    }

    @Test
    void generateAndUpdateMaintenanceChargeShouldWork() {
        ShieldPrincipal principal = principal();
        UUID id = UUID.randomUUID();

        MaintenanceChargeGenerateRequest createRequest = new MaintenanceChargeGenerateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(1000),
                "EQUAL_SHARE",
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000));

        when(maintenanceChargeRepository.save(any(MaintenanceChargeEntity.class))).thenAnswer(invocation -> {
            MaintenanceChargeEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(id);
            }
            return entity;
        });

        var created = billingManagementService.generateMaintenanceCharge(createRequest, principal);
        assertEquals(id, created.id());

        MaintenanceChargeEntity existing = new MaintenanceChargeEntity();
        existing.setId(id);
        existing.setTenantId(principal.tenantId());
        existing.setTotalAmount(BigDecimal.valueOf(1000));

        when(maintenanceChargeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existing));

        MaintenanceChargeUpdateRequest updateRequest = new MaintenanceChargeUpdateRequest(
                BigDecimal.valueOf(900),
                "HYBRID",
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(900));

        var updated = billingManagementService.updateMaintenanceCharge(id, updateRequest, principal);
        assertEquals(BigDecimal.valueOf(900), updated.totalAmount());

        billingManagementService.deleteMaintenanceCharge(id, principal);
        assertTrue(existing.isDeleted());
    }

    @Test
    void listMaintenanceChargesByCycleAndUnitShouldReturnData() {
        UUID cycleId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        MaintenanceChargeEntity byCycle = new MaintenanceChargeEntity();
        byCycle.setId(UUID.randomUUID());
        byCycle.setTenantId(UUID.randomUUID());
        byCycle.setBillingCycleId(cycleId);
        byCycle.setUnitId(unitId);
        byCycle.setTotalAmount(BigDecimal.valueOf(500));

        when(maintenanceChargeRepository.findAllByBillingCycleIdAndDeletedFalse(cycleId)).thenReturn(List.of(byCycle));
        when(maintenanceChargeRepository.findAllByUnitIdAndDeletedFalse(unitId)).thenReturn(List.of(byCycle));

        assertEquals(1, billingManagementService.listMaintenanceChargesByCycle(cycleId).size());
        assertEquals(1, billingManagementService.listMaintenanceChargesByUnit(unitId).size());
    }

    @Test
    void createAndListActiveSpecialAssessmentsShouldWork() {
        ShieldPrincipal principal = principal();

        SpecialAssessmentCreateRequest request = new SpecialAssessmentCreateRequest(
                "Elevator Upgrade",
                "Upgrade fund",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(500),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31));

        when(specialAssessmentRepository.save(any(SpecialAssessmentEntity.class))).thenAnswer(invocation -> {
            SpecialAssessmentEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        var created = billingManagementService.createSpecialAssessment(request, principal);
        assertEquals(SpecialAssessmentStatus.ACTIVE, created.status());
        assertEquals(principal.userId(), created.createdBy());

        SpecialAssessmentEntity activeEntity = new SpecialAssessmentEntity();
        activeEntity.setId(UUID.randomUUID());
        activeEntity.setTenantId(principal.tenantId());
        activeEntity.setAssessmentName("A");
        activeEntity.setTotalAmount(BigDecimal.ONE);
        activeEntity.setDueDate(LocalDate.now().plusDays(5));
        activeEntity.setStatus(SpecialAssessmentStatus.ACTIVE);

        when(specialAssessmentRepository.findAllByStatusAndDueDateGreaterThanEqualAndDeletedFalse(
                SpecialAssessmentStatus.ACTIVE,
                LocalDate.now(),
                PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(activeEntity), PageRequest.of(0, 10), 1));

        var active = billingManagementService.listActiveSpecialAssessments(PageRequest.of(0, 10));
        assertEquals(1, active.content().size());
    }

    @Test
    void updateSpecialAssessmentShouldRejectInvalidStatus() {
        ShieldPrincipal principal = principal();
        UUID id = UUID.randomUUID();

        SpecialAssessmentEntity entity = new SpecialAssessmentEntity();
        entity.setId(id);
        entity.setTenantId(principal.tenantId());

        SpecialAssessmentUpdateRequest request = new SpecialAssessmentUpdateRequest(
                "Elevator Fund",
                "Yearly maintenance",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(500),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                "UNKNOWN");

        when(specialAssessmentRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> billingManagementService.updateSpecialAssessment(id, request, principal));
    }

    @Test
    void deleteSpecialAssessmentShouldSoftDelete() {
        ShieldPrincipal principal = principal();
        UUID id = UUID.randomUUID();

        SpecialAssessmentEntity entity = new SpecialAssessmentEntity();
        entity.setId(id);
        entity.setTenantId(principal.tenantId());

        when(specialAssessmentRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(specialAssessmentRepository.save(any(SpecialAssessmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        billingManagementService.deleteSpecialAssessment(id, principal);

        assertTrue(entity.isDeleted());
    }

    @Test
    void generateInvoiceShouldComputeTotalsAndMarkUnpaid() {
        ShieldPrincipal principal = principal();
        InvoiceGenerateRequest request = new InvoiceGenerateRequest(
                UUID.randomUUID(),
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 10),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(180),
                BigDecimal.valueOf(0));

        when(invoiceRepository.findByInvoiceNumberAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> {
            InvoiceEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = billingManagementService.generateInvoice(request, principal);

        assertNotNull(response.id());
        assertEquals(InvoiceStatus.UNPAID, response.status());
        assertEquals(BigDecimal.valueOf(1205), response.totalAmount());
        assertEquals(BigDecimal.valueOf(1205), response.outstandingAmount());
    }

    @Test
    void bulkGenerateInvoicesShouldCreateInvoicesPerUnit() {
        ShieldPrincipal principal = principal();
        UUID unitOne = UUID.randomUUID();
        UUID unitTwo = UUID.randomUUID();

        InvoiceBulkGenerateRequest request = new InvoiceBulkGenerateRequest(
                List.of(unitOne, unitTwo),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 20),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(100),
                BigDecimal.ZERO);

        when(invoiceRepository.findByInvoiceNumberAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(invoiceRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<InvoiceEntity> entities = invocation.getArgument(0);
            entities.forEach(entity -> entity.setId(UUID.randomUUID()));
            return entities;
        });

        var response = billingManagementService.bulkGenerateInvoices(request, principal);

        assertEquals(2, response.size());
    }

    @Test
    void updateInvoiceShouldRejectInvalidStatus() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();

        InvoiceEntity entity = invoiceEntity();
        entity.setId(invoiceId);

        InvoiceUpdateRequest request = new InvoiceUpdateRequest(
                LocalDate.of(2026, 4, 15),
                BigDecimal.valueOf(1100),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1100),
                "INVALID_STATUS");

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> billingManagementService.updateInvoice(invoiceId, request, principal));
    }

    @Test
    void listInvoicesByStatusShouldRejectInvalidStatusValue() {
        PageRequest pageable = PageRequest.of(0, 10);
        assertThrows(BadRequestException.class, () -> billingManagementService.listInvoicesByStatus("something-else", pageable));
    }

    @Test
    void deleteInvoiceShouldSoftDelete() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();
        InvoiceEntity entity = invoiceEntity();
        entity.setId(invoiceId);

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(entity));
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        billingManagementService.deleteInvoice(invoiceId, principal);

        assertTrue(entity.isDeleted());
    }

    @Test
    void listDefaultersAndOutstandingShouldReturnPagedResponses() {
        InvoiceEntity invoice = invoiceEntity();
        invoice.setId(UUID.randomUUID());

        when(invoiceRepository.findAllByDueDateBeforeAndOutstandingAmountGreaterThanAndDeletedFalse(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(invoice), PageRequest.of(0, 10), 1));
        when(invoiceRepository.findAllByOutstandingAmountGreaterThanAndDeletedFalse(any(), any()))
                .thenReturn(new PageImpl<>(List.of(invoice), PageRequest.of(0, 10), 1));

        var defaulters = billingManagementService.listDefaulters(PageRequest.of(0, 10));
        var outstanding = billingManagementService.listOutstanding(PageRequest.of(0, 10));

        assertEquals(1, defaulters.content().size());
        assertEquals(1, outstanding.content().size());
    }

    @Test
    void sendPaymentReminderShouldPersistSentStatus() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();

        InvoiceEntity invoice = invoiceEntity();
        invoice.setId(invoiceId);

        PaymentReminderSendRequest request = new PaymentReminderSendRequest(invoiceId, "ON_DUE", "EMAIL");

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentReminderRepository.save(any())).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, PaymentReminderEntity.class);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        PaymentReminderResponse response = billingManagementService.sendPaymentReminder(request, principal);

        assertEquals("ON_DUE", response.reminderType());
        assertEquals("EMAIL", response.channel());
        assertEquals(ReminderStatus.SENT, response.status());
        assertNotNull(response.sentAt());
    }

    @Test
    void schedulePaymentReminderShouldPersistScheduledStatus() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();
        Instant scheduledAt = Instant.parse("2026-03-05T10:00:00Z");
        PaymentReminderScheduleRequest request = new PaymentReminderScheduleRequest(invoiceId, "BEFORE_DUE", "EMAIL", scheduledAt);

        InvoiceEntity invoiceEntity = invoiceEntity();
        invoiceEntity.setId(invoiceId);
        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoiceEntity));

        when(paymentReminderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentReminderResponse response = billingManagementService.schedulePaymentReminder(request, principal);

        assertEquals("BEFORE_DUE", response.reminderType());
        assertEquals(scheduledAt, response.sentAt());
        assertEquals(ReminderStatus.SCHEDULED, response.status());
    }

    @Test
    void listPaymentRemindersShouldReturnPagedAndInvoiceLists() {
        UUID invoiceId = UUID.randomUUID();

        PaymentReminderEntity reminder = new PaymentReminderEntity();
        reminder.setId(UUID.randomUUID());
        reminder.setTenantId(UUID.randomUUID());
        reminder.setInvoiceId(invoiceId);
        reminder.setReminderType("ON_DUE");
        reminder.setChannel("EMAIL");
        reminder.setStatus(ReminderStatus.SENT);

        when(paymentReminderRepository.findAllByDeletedFalse(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(reminder), PageRequest.of(0, 10), 1));
        when(paymentReminderRepository.findAllByInvoiceIdAndDeletedFalse(invoiceId)).thenReturn(List.of(reminder));

        var paged = billingManagementService.listPaymentReminders(PageRequest.of(0, 10));
        var byInvoice = billingManagementService.listPaymentRemindersByInvoice(invoiceId);

        assertEquals(1, paged.content().size());
        assertEquals(1, byInvoice.size());
    }

    @Test
    void createLateFeeRuleShouldParseFeeTypeAndSetActive() {
        ShieldPrincipal principal = principal();

        LateFeeRuleCreateRequest request = new LateFeeRuleCreateRequest(
                "Standard Late Fee",
                5,
                "PERCENTAGE",
                BigDecimal.valueOf(2.5));

        when(lateFeeRuleRepository.save(any())).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, LateFeeRuleEntity.class);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = billingManagementService.createLateFeeRule(request, principal);

        assertEquals("Standard Late Fee", response.ruleName());
        assertEquals(LateFeeType.PERCENTAGE, response.feeType());
        assertTrue(response.active());
    }

    @Test
    void updateActivateDeactivateAndDeleteLateFeeRuleShouldWork() {
        ShieldPrincipal principal = principal();
        UUID id = UUID.randomUUID();

        LateFeeRuleEntity entity = new LateFeeRuleEntity();
        entity.setId(id);
        entity.setTenantId(principal.tenantId());
        entity.setRuleName("Rule");
        entity.setDaysAfterDue(3);
        entity.setFeeType(LateFeeType.FLAT);
        entity.setFeeAmount(BigDecimal.valueOf(50));
        entity.setActive(true);

        when(lateFeeRuleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(lateFeeRuleRepository.save(any(LateFeeRuleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = billingManagementService.updateLateFeeRule(
                id,
                new LateFeeRuleUpdateRequest("Updated Rule", 6, "PERCENTAGE", BigDecimal.valueOf(3), false),
                principal);
        assertEquals("Updated Rule", updated.ruleName());
        assertEquals(LateFeeType.PERCENTAGE, updated.feeType());
        assertEquals(false, updated.active());

        var deactivated = billingManagementService.deactivateLateFeeRule(id, principal);
        assertEquals(false, deactivated.active());

        var activated = billingManagementService.activateLateFeeRule(id, principal);
        assertEquals(true, activated.active());

        billingManagementService.deleteLateFeeRule(id, principal);
        assertTrue(entity.isDeleted());
    }

    @Test
    void createLateFeeRuleShouldRejectInvalidFeeType() {
        ShieldPrincipal principal = principal();

        LateFeeRuleCreateRequest request = new LateFeeRuleCreateRequest(
                "Invalid Fee Type",
                5,
                "RANDOM",
                BigDecimal.ONE);

        assertThrows(BadRequestException.class, () -> billingManagementService.createLateFeeRule(request, principal));
    }

    private ShieldPrincipal principal() {
        return new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
    }

    private BillingCycleEntity billingCycle(BillingCycleStatus status) {
        BillingCycleEntity entity = new BillingCycleEntity();
        entity.setTenantId(UUID.randomUUID());
        entity.setCycleName("Cycle");
        entity.setMonth(3);
        entity.setYear(2026);
        entity.setDueDate(LocalDate.of(2026, 3, 31));
        entity.setLateFeeApplicableDate(LocalDate.of(2026, 4, 5));
        entity.setStatus(status);
        return entity;
    }

    private InvoiceEntity invoiceEntity() {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setTenantId(UUID.randomUUID());
        entity.setInvoiceNumber("INV-2026-ABC12345");
        entity.setUnitId(UUID.randomUUID());
        entity.setInvoiceDate(LocalDate.of(2026, 3, 1));
        entity.setDueDate(LocalDate.of(2026, 3, 31));
        entity.setSubtotal(BigDecimal.valueOf(1000));
        entity.setLateFee(BigDecimal.ZERO);
        entity.setGstAmount(BigDecimal.ZERO);
        entity.setOtherCharges(BigDecimal.ZERO);
        entity.setTotalAmount(BigDecimal.valueOf(1000));
        entity.setOutstandingAmount(BigDecimal.valueOf(1000));
        entity.setStatus(InvoiceStatus.UNPAID);
        return entity;
    }
}
