package com.shield.module.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.billing.dto.PaymentCashRequest;
import com.shield.module.billing.dto.PaymentChequeRequest;
import com.shield.module.billing.dto.PaymentRefundRequest;
import com.shield.module.billing.entity.InvoiceEntity;
import com.shield.module.billing.entity.InvoiceStatus;
import com.shield.module.billing.entity.PaymentEntity;
import com.shield.module.billing.repository.InvoiceRepository;
import com.shield.module.billing.repository.PaymentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
class PaymentOperationsServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AuditLogService auditLogService;

    private PaymentOperationsService paymentOperationsService;

    @BeforeEach
    void setUp() {
        paymentOperationsService = new PaymentOperationsService(paymentRepository, invoiceRepository, auditLogService);
    }

    @Test
    void createCashPaymentShouldPersistAndUpdateInvoiceOutstanding() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();

        InvoiceEntity invoice = invoice(invoiceId, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByTransactionRefAndDeletedFalse("CASH-TXN-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentOperationsService.createCashPayment(
                new PaymentCashRequest(invoiceId, BigDecimal.valueOf(400), "CASH-TXN-1"),
                principal);

        assertEquals("SUCCESS", response.paymentStatus());
        assertEquals("CASH", response.mode());
        assertNotNull(response.receiptUrl());
        assertEquals(BigDecimal.valueOf(600).setScale(2), invoice.getOutstandingAmount().setScale(2, RoundingMode.HALF_UP));
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice.getStatus());
    }

    @Test
    void createChequePaymentShouldGenerateTransactionReferenceWhenMissing() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();

        InvoiceEntity invoice = invoice(invoiceId, BigDecimal.valueOf(900), BigDecimal.valueOf(900));

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByTransactionRefAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentOperationsService.createChequePayment(
                new PaymentChequeRequest(invoiceId, BigDecimal.valueOf(300), "chk001", null),
                principal);

        assertEquals("CHEQUE", response.mode());
        assertTrue(response.transactionRef().startsWith("CHEQUE-CHK001-"));
    }

    @Test
    void createPaymentShouldRejectWhenAmountExceedsOutstanding() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();

        InvoiceEntity invoice = invoice(invoiceId, BigDecimal.valueOf(1000), BigDecimal.valueOf(250));

        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoice));

        PaymentCashRequest request = new PaymentCashRequest(invoiceId, BigDecimal.valueOf(300), "CASH-TXN-2");
        assertThrows(BadRequestException.class, () -> paymentOperationsService.createCashPayment(request, principal));
    }

    @Test
    void refundShouldMarkPaymentRefundedAndIncreaseOutstanding() {
        ShieldPrincipal principal = principal();
        UUID invoiceId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentEntity payment = new PaymentEntity();
        payment.setId(paymentId);
        payment.setTenantId(principal.tenantId());
        payment.setInvoiceId(invoiceId);
        payment.setUnitId(UUID.randomUUID());
        payment.setAmount(BigDecimal.valueOf(400));
        payment.setMode("CHEQUE");
        payment.setPaymentStatus("SUCCESS");

        InvoiceEntity invoice = invoice(invoiceId, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.PAID);

        when(paymentRepository.findByIdAndDeletedFalse(paymentId)).thenReturn(Optional.of(payment));
        when(invoiceRepository.findByIdAndDeletedFalse(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentOperationsService.refund(paymentId, new PaymentRefundRequest("Cheque bounced"), principal);

        assertEquals("REFUNDED", response.paymentStatus());
        assertNotNull(response.refundedAt());
        assertEquals(BigDecimal.valueOf(400).setScale(2), invoice.getOutstandingAmount().setScale(2, RoundingMode.HALF_UP));
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice.getStatus());
    }

    @Test
    void refundShouldRejectWhenPaymentAlreadyRefunded() {
        ShieldPrincipal principal = principal();
        UUID paymentId = UUID.randomUUID();

        PaymentEntity payment = new PaymentEntity();
        payment.setId(paymentId);
        payment.setInvoiceId(UUID.randomUUID());
        payment.setPaymentStatus("REFUNDED");

        when(paymentRepository.findByIdAndDeletedFalse(paymentId)).thenReturn(Optional.of(payment));

        PaymentRefundRequest request = new PaymentRefundRequest("Duplicate");
        assertThrows(BadRequestException.class, () -> paymentOperationsService.refund(paymentId, request, principal));
    }

    @Test
    void refundShouldRejectWhenPaymentIsNotInvoiceLinked() {
        ShieldPrincipal principal = principal();
        UUID paymentId = UUID.randomUUID();

        PaymentEntity payment = new PaymentEntity();
        payment.setId(paymentId);
        payment.setPaymentStatus("SUCCESS");
        payment.setInvoiceId(null);

        when(paymentRepository.findByIdAndDeletedFalse(paymentId)).thenReturn(Optional.of(payment));

        PaymentRefundRequest request = new PaymentRefundRequest("Unsupported");
        assertThrows(BadRequestException.class, () -> paymentOperationsService.refund(paymentId, request, principal));
    }

    @Test
    void listAndReceiptApisShouldReturnMappedResponses() {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        PaymentEntity payment = new PaymentEntity();
        payment.setId(paymentId);
        payment.setTenantId(UUID.randomUUID());
        payment.setInvoiceId(invoiceId);
        payment.setUnitId(unitId);
        payment.setAmount(BigDecimal.valueOf(550));
        payment.setMode("CASH");
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionRef("CASH-TXN-3");
        payment.setReceiptUrl("receipt://payment/" + paymentId);

        when(paymentRepository.findAllByDeletedFalse(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(payment), PageRequest.of(0, 10), 1));
        when(paymentRepository.findAllByInvoiceIdAndDeletedFalse(invoiceId)).thenReturn(List.of(payment));
        when(paymentRepository.findAllByUnitIdAndDeletedFalse(unitId)).thenReturn(List.of(payment));
        when(paymentRepository.findByIdAndDeletedFalse(paymentId)).thenReturn(Optional.of(payment));

        var listResponse = paymentOperationsService.list(PageRequest.of(0, 10));
        var byInvoice = paymentOperationsService.listByInvoice(invoiceId);
        var byUnit = paymentOperationsService.listByUnit(unitId);
        var receipt = paymentOperationsService.getReceipt(paymentId);

        assertEquals(1, listResponse.content().size());
        assertEquals(1, byInvoice.size());
        assertEquals(1, byUnit.size());
        assertEquals(paymentId, receipt.paymentId());
        assertTrue(receipt.receiptUrl().contains(paymentId.toString()));
    }

    private ShieldPrincipal principal() {
        return new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
    }

    private InvoiceEntity invoice(UUID invoiceId, BigDecimal total, BigDecimal outstanding) {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(invoiceId);
        invoice.setTenantId(UUID.randomUUID());
        invoice.setUnitId(UUID.randomUUID());
        invoice.setInvoiceNumber("INV-2026-UNIT");
        invoice.setInvoiceDate(LocalDate.of(2026, 3, 1));
        invoice.setDueDate(LocalDate.of(2026, 3, 31));
        invoice.setSubtotal(total);
        invoice.setLateFee(BigDecimal.ZERO);
        invoice.setGstAmount(BigDecimal.ZERO);
        invoice.setOtherCharges(BigDecimal.ZERO);
        invoice.setTotalAmount(total);
        invoice.setOutstandingAmount(outstanding);
        invoice.setStatus(InvoiceStatus.UNPAID);
        return invoice;
    }
}
