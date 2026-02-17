package com.shield.module.billing.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.billing.dto.BillGenerateRequest;
import com.shield.module.billing.dto.BillResponse;
import com.shield.module.billing.dto.PaymentCreateRequest;
import com.shield.module.billing.dto.PaymentResponse;
import com.shield.module.billing.entity.BillStatus;
import com.shield.module.billing.entity.MaintenanceBillEntity;
import com.shield.module.billing.entity.PaymentEntity;
import com.shield.module.billing.repository.MaintenanceBillRepository;
import com.shield.module.billing.repository.PaymentRepository;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final MaintenanceBillRepository maintenanceBillRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public BillResponse generate(BillGenerateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        MaintenanceBillEntity bill = new MaintenanceBillEntity();
        bill.setTenantId(tenantId);
        bill.setUnitId(request.unitId());
        bill.setMonth(request.month());
        bill.setYear(request.year());
        bill.setAmount(request.amount());
        bill.setDueDate(request.dueDate());
        bill.setLateFee(request.lateFee());
        bill.setStatus(BillStatus.PENDING);

        MaintenanceBillEntity saved = maintenanceBillRepository.save(bill);
        auditLogService.record(tenantId, null, "BILL_GENERATED", "maintenance_bill", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getByUnit(UUID unitId) {
        return maintenanceBillRepository.findByUnitIdAndDeletedFalse(unitId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PaymentResponse createPayment(PaymentCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        MaintenanceBillEntity bill = maintenanceBillRepository.findByIdAndDeletedFalse(request.billId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + request.billId()));

        PaymentEntity payment = new PaymentEntity();
        payment.setTenantId(tenantId);
        payment.setBillId(request.billId());
        payment.setAmount(request.amount());
        payment.setMode(request.mode());
        payment.setTransactionRef(request.transactionRef());
        payment.setPaidAt(Instant.now());

        PaymentEntity savedPayment = paymentRepository.save(payment);
        bill.setStatus(BillStatus.PAID);
        maintenanceBillRepository.save(bill);

        auditLogService.record(tenantId, null, "PAYMENT_CREATED", "payment", savedPayment.getId(), null);
        return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        PaymentEntity payment = paymentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return toResponse(payment);
    }

    private BillResponse toResponse(MaintenanceBillEntity bill) {
        return new BillResponse(
                bill.getId(),
                bill.getTenantId(),
                bill.getUnitId(),
                bill.getMonth(),
                bill.getYear(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getStatus(),
                bill.getLateFee());
    }

    private PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTenantId(),
                payment.getBillId(),
                payment.getAmount(),
                payment.getMode(),
                payment.getTransactionRef(),
                payment.getPaidAt());
    }
}
