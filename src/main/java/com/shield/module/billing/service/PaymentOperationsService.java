package com.shield.module.billing.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.billing.dto.PaymentCashRequest;
import com.shield.module.billing.dto.PaymentChequeRequest;
import com.shield.module.billing.dto.PaymentReceiptResponse;
import com.shield.module.billing.dto.PaymentRefundRequest;
import com.shield.module.billing.dto.PaymentResponse;
import com.shield.module.billing.entity.InvoiceEntity;
import com.shield.module.billing.entity.InvoiceStatus;
import com.shield.module.billing.entity.PaymentEntity;
import com.shield.module.billing.repository.InvoiceRepository;
import com.shield.module.billing.repository.PaymentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
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
public class PaymentOperationsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> list(Pageable pageable) {
        return PagedResponse.from(paymentRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByInvoice(UUID invoiceId) {
        return paymentRepository.findAllByInvoiceIdAndDeletedFalse(invoiceId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByUnit(UUID unitId) {
        return paymentRepository.findAllByUnitIdAndDeletedFalse(unitId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentReceiptResponse getReceipt(UUID paymentId) {
        PaymentEntity entity = findPayment(paymentId);
        return new PaymentReceiptResponse(entity.getId(), entity.getReceiptUrl());
    }

    public PaymentResponse createCashPayment(PaymentCashRequest request, ShieldPrincipal principal) {
        String txnRef = normalizeRef(request.transactionRef(), "CASH");
        return createInvoicePayment(request.invoiceId(), request.amount(), "CASH", txnRef, principal, "OFFLINE");
    }

    public PaymentResponse createChequePayment(PaymentChequeRequest request, ShieldPrincipal principal) {
        String txnRef = normalizeRef(
                request.transactionRef(),
                "CHEQUE-" + request.chequeNumber().trim().toUpperCase(Locale.ROOT));
        return createInvoicePayment(request.invoiceId(), request.amount(), "CHEQUE", txnRef, principal, "OFFLINE");
    }

    public PaymentResponse refund(UUID paymentId, PaymentRefundRequest request, ShieldPrincipal principal) {
        PaymentEntity payment = findPayment(paymentId);
        if (payment.getInvoiceId() == null) {
            throw new BadRequestException("Refund is supported only for invoice payments");
        }
        if ("REFUNDED".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new BadRequestException("Payment already refunded");
        }

        InvoiceEntity invoice = invoiceRepository.findByIdAndDeletedFalse(payment.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + payment.getInvoiceId()));

        payment.setPaymentStatus("REFUNDED");
        payment.setRefundedAt(Instant.now());
        payment.setRefundReason(request.reason().trim());
        paymentRepository.save(payment);

        BigDecimal updatedOutstanding = invoice.getOutstandingAmount().add(payment.getAmount());
        invoice.setOutstandingAmount(updatedOutstanding);
        invoice.setStatus(resolveInvoiceStatus(updatedOutstanding, invoice.getTotalAmount(), invoice.getDueDate()));
        invoiceRepository.save(invoice);

        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYMENT_REFUNDED", "payment", paymentId, null);
        return toResponse(payment);
    }

    private PaymentResponse createInvoicePayment(
            UUID invoiceId,
            BigDecimal amount,
            String mode,
            String transactionRef,
            ShieldPrincipal principal,
            String gateway) {
        InvoiceEntity invoice = invoiceRepository.findByIdAndDeletedFalse(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (amount.compareTo(ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be positive");
        }
        if (amount.compareTo(invoice.getOutstandingAmount()) > 0) {
            throw new BadRequestException("Payment amount cannot exceed outstanding amount");
        }
        if (paymentRepository.findByTransactionRefAndDeletedFalse(transactionRef).isPresent()) {
            throw new BadRequestException("Duplicate payment transaction reference");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setTenantId(principal.tenantId());
        payment.setInvoiceId(invoiceId);
        payment.setUnitId(invoice.getUnitId());
        payment.setAmount(amount);
        payment.setMode(mode);
        payment.setTransactionRef(transactionRef);
        payment.setPaidAt(Instant.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentGateway(gateway);

        PaymentEntity saved = paymentRepository.save(payment);
        saved.setReceiptUrl("receipt://payment/" + saved.getId());
        saved = paymentRepository.save(saved);

        BigDecimal updatedOutstanding = invoice.getOutstandingAmount().subtract(amount);
        invoice.setOutstandingAmount(updatedOutstanding);
        invoice.setStatus(resolveInvoiceStatus(updatedOutstanding, invoice.getTotalAmount(), invoice.getDueDate()));
        invoiceRepository.save(invoice);

        auditLogService.logEvent(principal.tenantId(), principal.userId(), "PAYMENT_CREATED", "payment", saved.getId(), null);
        return toResponse(saved);
    }

    private String normalizeRef(String provided, String prefix) {
        if (provided != null && !provided.isBlank()) {
            return provided.trim();
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return prefix + "-" + suffix;
    }

    private InvoiceStatus resolveInvoiceStatus(BigDecimal outstanding, BigDecimal total, java.time.LocalDate dueDate) {
        if (outstanding.compareTo(ZERO) <= 0) {
            return InvoiceStatus.PAID;
        }
        if (outstanding.compareTo(total) < 0) {
            return InvoiceStatus.PARTIALLY_PAID;
        }
        if (dueDate != null && dueDate.isBefore(java.time.LocalDate.now())) {
            return InvoiceStatus.OVERDUE;
        }
        return InvoiceStatus.UNPAID;
    }

    private PaymentEntity findPayment(UUID paymentId) {
        return paymentRepository.findByIdAndDeletedFalse(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
    }

    private PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTenantId(),
                payment.getBillId(),
                payment.getInvoiceId(),
                payment.getUnitId(),
                payment.getAmount(),
                payment.getMode(),
                payment.getPaymentStatus(),
                payment.getTransactionRef(),
                payment.getReceiptUrl(),
                payment.getPaidAt(),
                payment.getRefundedAt());
    }
}
