package com.shield.module.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.billing.dto.PaymentInitiateRequest;
import com.shield.module.billing.dto.PaymentInitiateResponse;
import com.shield.module.billing.dto.PaymentVerifyRequest;
import com.shield.module.billing.entity.BillStatus;
import com.shield.module.billing.entity.MaintenanceBillEntity;
import com.shield.module.billing.entity.PaymentEntity;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import com.shield.module.billing.entity.PaymentGatewayTransactionStatus;
import com.shield.module.billing.repository.MaintenanceBillRepository;
import com.shield.module.billing.repository.PaymentGatewayTransactionRepository;
import com.shield.module.billing.repository.PaymentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private PaymentGatewayTransactionRepository paymentGatewayTransactionRepository;

    @Mock
    private MaintenanceBillRepository maintenanceBillRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditLogService auditLogService;

    private PaymentGatewayService paymentGatewayService;

    @BeforeEach
    void setUp() {
        paymentGatewayService = new PaymentGatewayService(
                paymentGatewayTransactionRepository,
                maintenanceBillRepository,
                paymentRepository,
                auditLogService);
    }

    @Test
    void initiateShouldCreateGatewayTransactionAndReturnCheckoutToken() {
        UUID tenantId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();

        MaintenanceBillEntity bill = new MaintenanceBillEntity();
        bill.setId(billId);
        bill.setTenantId(tenantId);
        bill.setAmount(BigDecimal.valueOf(2500));
        bill.setStatus(BillStatus.PENDING);

        when(maintenanceBillRepository.findByIdAndDeletedFalse(billId)).thenReturn(Optional.of(bill));
        when(paymentGatewayTransactionRepository.save(any(PaymentGatewayTransactionEntity.class))).thenAnswer(invocation -> {
            PaymentGatewayTransactionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        PaymentInitiateRequest request = new PaymentInitiateRequest(billId, BigDecimal.valueOf(2500), "UPI", "razorpay");

        PaymentInitiateResponse response = paymentGatewayService.initiate(request, principal);

        assertTrue(response.transactionRef().startsWith("PGTXN-"));
        assertTrue(response.gatewayOrderId().startsWith("ORD-"));
        assertEquals("RAZORPAY", response.provider());
        assertNotNull(response.checkoutToken());
    }

    @Test
    void verifyShouldCreatePaymentAndMarkBillPaidOnSuccess() {
        UUID tenantId = UUID.randomUUID();
        UUID billId = UUID.randomUUID();
        String transactionRef = "PGTXN-TEST123";

        PaymentGatewayTransactionEntity transaction = new PaymentGatewayTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setTenantId(tenantId);
        transaction.setBillId(billId);
        transaction.setTransactionRef(transactionRef);
        transaction.setAmount(BigDecimal.valueOf(1800));
        transaction.setMode("UPI");
        transaction.setCurrency("INR");
        transaction.setStatus(PaymentGatewayTransactionStatus.CREATED);

        MaintenanceBillEntity bill = new MaintenanceBillEntity();
        bill.setId(billId);
        bill.setTenantId(tenantId);
        bill.setAmount(BigDecimal.valueOf(1800));
        bill.setStatus(BillStatus.PENDING);

        when(paymentGatewayTransactionRepository.findByTransactionRefAndDeletedFalse(transactionRef)).thenReturn(Optional.of(transaction));
        when(paymentGatewayTransactionRepository.save(any(PaymentGatewayTransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByTransactionRefAndDeletedFalse(transactionRef)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(maintenanceBillRepository.findByIdAndDeletedFalse(billId)).thenReturn(Optional.of(bill));
        when(maintenanceBillRepository.save(any(MaintenanceBillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "committee@shield.dev", "COMMITTEE");

        var response = paymentGatewayService.verify(new PaymentVerifyRequest(transactionRef, "pay_123", true, null), principal);

        assertEquals(PaymentGatewayTransactionStatus.SUCCESS, response.status());
        assertNotNull(response.paymentId());
        assertEquals(BillStatus.PAID, bill.getStatus());
    }

    @Test
    void verifyShouldMarkFailureWithoutCreatingPayment() {
        UUID tenantId = UUID.randomUUID();
        String transactionRef = "PGTXN-FAILED1";

        PaymentGatewayTransactionEntity transaction = new PaymentGatewayTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setTenantId(tenantId);
        transaction.setBillId(UUID.randomUUID());
        transaction.setTransactionRef(transactionRef);
        transaction.setAmount(BigDecimal.valueOf(1200));
        transaction.setMode("CARD");
        transaction.setCurrency("INR");
        transaction.setStatus(PaymentGatewayTransactionStatus.CREATED);

        when(paymentGatewayTransactionRepository.findByTransactionRefAndDeletedFalse(transactionRef)).thenReturn(Optional.of(transaction));
        when(paymentGatewayTransactionRepository.save(any(PaymentGatewayTransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByTransactionRefAndDeletedFalse(transactionRef)).thenReturn(Optional.empty());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");

        var response = paymentGatewayService.verify(
                new PaymentVerifyRequest(transactionRef, null, false, "Gateway declined"),
                principal);

        assertEquals(PaymentGatewayTransactionStatus.FAILED, response.status());
        assertEquals("Gateway declined", response.failureReason());
    }
}
