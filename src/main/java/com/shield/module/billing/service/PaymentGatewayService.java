package com.shield.module.billing.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.dto.PaymentGatewayTransactionResponse;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentGatewayService {

    private static final String DEFAULT_PROVIDER = "MANUAL_SIMULATOR";
    private static final String DEFAULT_CURRENCY = "INR";

    private final PaymentGatewayTransactionRepository paymentGatewayTransactionRepository;
    private final MaintenanceBillRepository maintenanceBillRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public PaymentGatewayService(
            PaymentGatewayTransactionRepository paymentGatewayTransactionRepository,
            MaintenanceBillRepository maintenanceBillRepository,
            PaymentRepository paymentRepository,
            AuditLogService auditLogService) {
        this.paymentGatewayTransactionRepository = paymentGatewayTransactionRepository;
        this.maintenanceBillRepository = maintenanceBillRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
    }

    public PaymentInitiateResponse initiate(PaymentInitiateRequest request, ShieldPrincipal principal) {
        MaintenanceBillEntity bill = maintenanceBillRepository.findByIdAndDeletedFalse(request.billId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + request.billId()));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already paid");
        }

        if (request.amount().compareTo(bill.getAmount()) != 0) {
            throw new BadRequestException("Payment amount must match bill amount for gateway initiation");
        }

        String transactionRef = "PGTXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        String gatewayOrderId = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        String provider = normalizeProvider(request.provider());
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        PaymentGatewayTransactionEntity entity = new PaymentGatewayTransactionEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTransactionRef(transactionRef);
        entity.setBillId(request.billId());
        entity.setProvider(provider);
        entity.setGatewayOrderId(gatewayOrderId);
        entity.setAmount(request.amount());
        entity.setCurrency(DEFAULT_CURRENCY);
        entity.setMode(request.mode().trim().toUpperCase(Locale.ROOT));
        entity.setStatus(PaymentGatewayTransactionStatus.CREATED);
        entity.setInitiatedBy(principal.userId());

        PaymentGatewayTransactionEntity saved = paymentGatewayTransactionRepository.save(entity);

        auditLogService.record(
                principal.tenantId(),
                principal.userId(),
                "PAYMENT_GATEWAY_INITIATED",
                "payment_gateway_txn",
                saved.getId(),
                null);

        String checkoutToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((transactionRef + ":" + gatewayOrderId).getBytes(StandardCharsets.UTF_8));

        return new PaymentInitiateResponse(
                saved.getTransactionRef(),
                saved.getGatewayOrderId(),
                saved.getProvider(),
                saved.getAmount(),
                saved.getCurrency(),
                checkoutToken,
                expiresAt);
    }

    public PaymentGatewayTransactionResponse verify(PaymentVerifyRequest request, ShieldPrincipal principal) {
        PaymentGatewayTransactionEntity transaction = findByTransactionRef(request.transactionRef());

        if (request.success()) {
            if (request.gatewayPaymentId() != null && !request.gatewayPaymentId().isBlank()) {
                transaction.setGatewayPaymentId(request.gatewayPaymentId().trim());
            }

            transaction.setStatus(PaymentGatewayTransactionStatus.SUCCESS);
            transaction.setFailureReason(null);
            transaction.setVerifiedBy(principal.userId());
            transaction.setVerifiedAt(Instant.now());

            PaymentGatewayTransactionEntity savedTransaction = paymentGatewayTransactionRepository.save(transaction);
            PaymentEntity payment = getOrCreatePayment(savedTransaction);

            auditLogService.record(
                    principal.tenantId(),
                    principal.userId(),
                    "PAYMENT_GATEWAY_VERIFIED_SUCCESS",
                    "payment_gateway_txn",
                    savedTransaction.getId(),
                    null);

            return toResponse(savedTransaction, payment.getId());
        }

        if (transaction.getStatus() == PaymentGatewayTransactionStatus.SUCCESS) {
            throw new BadRequestException("Cannot mark a successful transaction as failed");
        }

        transaction.setStatus(PaymentGatewayTransactionStatus.FAILED);
        transaction.setFailureReason(resolveFailureReason(request.failureReason()));
        transaction.setVerifiedBy(principal.userId());
        transaction.setVerifiedAt(Instant.now());

        PaymentGatewayTransactionEntity savedTransaction = paymentGatewayTransactionRepository.save(transaction);

        auditLogService.record(
                principal.tenantId(),
                principal.userId(),
                "PAYMENT_GATEWAY_VERIFIED_FAILED",
                "payment_gateway_txn",
                savedTransaction.getId(),
                null);

        return toResponse(savedTransaction, findPaymentId(savedTransaction.getTransactionRef()));
    }

    public PaymentGatewayTransactionResponse callback(PaymentCallbackRequest request, ShieldPrincipal principal) {
        PaymentGatewayTransactionEntity transaction = findByTransactionRef(request.transactionRef());

        if (request.gatewayOrderId() != null && !request.gatewayOrderId().isBlank()) {
            transaction.setGatewayOrderId(request.gatewayOrderId().trim());
        }
        if (request.payload() != null && !request.payload().isBlank()) {
            transaction.setCallbackPayload(request.payload());
        }

        paymentGatewayTransactionRepository.save(transaction);

        boolean success = "SUCCESS".equalsIgnoreCase(request.status()) || "PAID".equalsIgnoreCase(request.status());
        String failureReason = success ? null : "Callback status: " + request.status();

        PaymentVerifyRequest verifyRequest = new PaymentVerifyRequest(
                request.transactionRef(),
                request.gatewayPaymentId(),
                success,
                failureReason);

        return verify(verifyRequest, principal);
    }

    @Transactional(readOnly = true)
    public PaymentGatewayTransactionResponse getByTransactionRef(String transactionRef) {
        PaymentGatewayTransactionEntity transaction = findByTransactionRef(transactionRef);
        return toResponse(transaction, findPaymentId(transaction.getTransactionRef()));
    }

    private PaymentEntity getOrCreatePayment(PaymentGatewayTransactionEntity transaction) {
        var existing = paymentRepository.findByTransactionRefAndDeletedFalse(transaction.getTransactionRef());
        if (existing.isPresent()) {
            return existing.get();
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setTenantId(transaction.getTenantId());
        payment.setBillId(transaction.getBillId());
        payment.setAmount(transaction.getAmount());
        payment.setMode(transaction.getMode());
        payment.setTransactionRef(transaction.getTransactionRef());
        payment.setPaidAt(Instant.now());

        PaymentEntity savedPayment = paymentRepository.save(payment);

        MaintenanceBillEntity bill = maintenanceBillRepository.findByIdAndDeletedFalse(transaction.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + transaction.getBillId()));
        bill.setStatus(BillStatus.PAID);
        maintenanceBillRepository.save(bill);

        return savedPayment;
    }

    private UUID findPaymentId(String transactionRef) {
        return paymentRepository.findByTransactionRefAndDeletedFalse(transactionRef)
                .map(PaymentEntity::getId)
                .orElse(null);
    }

    private PaymentGatewayTransactionEntity findByTransactionRef(String transactionRef) {
        return paymentGatewayTransactionRepository.findByTransactionRefAndDeletedFalse(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found: " + transactionRef));
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return DEFAULT_PROVIDER;
        }
        return provider.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Verification failed";
        }
        return failureReason.trim();
    }

    private PaymentGatewayTransactionResponse toResponse(PaymentGatewayTransactionEntity entity, UUID paymentId) {
        return new PaymentGatewayTransactionResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getBillId(),
                entity.getTransactionRef(),
                entity.getProvider(),
                entity.getGatewayOrderId(),
                entity.getGatewayPaymentId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getMode(),
                entity.getStatus(),
                entity.getFailureReason(),
                paymentId,
                entity.getVerifiedAt(),
                entity.getCreatedAt());
    }
}
