package com.shield.module.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.billing.dto.PaymentCreateRequest;
import com.shield.module.billing.dto.PaymentResponse;
import com.shield.module.billing.entity.BillStatus;
import com.shield.module.billing.entity.MaintenanceBillEntity;
import com.shield.module.billing.entity.PaymentEntity;
import com.shield.module.billing.repository.MaintenanceBillRepository;
import com.shield.module.billing.repository.PaymentRepository;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private MaintenanceBillRepository maintenanceBillRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditLogService auditLogService;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(maintenanceBillRepository, paymentRepository, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createPaymentShouldPersistAndMarkBillPaid() {
        UUID tenantId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        MaintenanceBillEntity bill = new MaintenanceBillEntity();
        bill.setId(billId);
        bill.setTenantId(tenantId);
        bill.setAmount(BigDecimal.valueOf(2000));
        bill.setStatus(BillStatus.PENDING);

        when(maintenanceBillRepository.findByIdAndDeletedFalse(billId)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });
        when(maintenanceBillRepository.save(any(MaintenanceBillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCreateRequest request = new PaymentCreateRequest(billId, BigDecimal.valueOf(2000), "UPI", "UPI-REF-1");

        PaymentResponse response = billingService.createPayment(request);

        assertEquals(tenantId, response.tenantId());
        assertEquals("UPI", response.mode());
        assertEquals(BillStatus.PAID, bill.getStatus());
    }

    @Test
    void createPaymentShouldRejectDuplicateTransactionReference() {
        UUID tenantId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        MaintenanceBillEntity bill = new MaintenanceBillEntity();
        bill.setId(billId);
        bill.setTenantId(tenantId);
        bill.setAmount(BigDecimal.valueOf(500));
        bill.setStatus(BillStatus.PENDING);

        when(maintenanceBillRepository.findByIdAndDeletedFalse(billId)).thenReturn(Optional.of(bill));

        PaymentEntity existingPayment = new PaymentEntity();
        existingPayment.setId(UUID.randomUUID());
        when(paymentRepository.findByTransactionRefAndDeletedFalse("DUP-REF")).thenReturn(Optional.of(existingPayment));

        PaymentCreateRequest request = new PaymentCreateRequest(billId, BigDecimal.valueOf(500), "CARD", "DUP-REF");

        assertThrows(BadRequestException.class, () -> billingService.createPayment(request));
    }
}
