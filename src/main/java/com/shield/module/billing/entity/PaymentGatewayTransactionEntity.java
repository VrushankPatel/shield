package com.shield.module.billing.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_gateway_txn")
public class PaymentGatewayTransactionEntity extends TenantAwareEntity {

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 120)
    private String transactionRef;

    @Column(name = "bill_id", nullable = false, columnDefinition = "uuid")
    private UUID billId;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "gateway_order_id", length = 120)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 120)
    private String gatewayPaymentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 50)
    private String mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentGatewayTransactionStatus status;

    @Column(name = "callback_payload")
    private String callbackPayload;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "initiated_by", columnDefinition = "uuid")
    private UUID initiatedBy;

    @Column(name = "verified_by", columnDefinition = "uuid")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
