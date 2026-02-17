package com.shield.module.billing.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class PaymentEntity extends TenantAwareEntity {

    @Column(name = "bill_id", nullable = false, columnDefinition = "uuid")
    private UUID billId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String mode;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "paid_at")
    private Instant paidAt;
}
