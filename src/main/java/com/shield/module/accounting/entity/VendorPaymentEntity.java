package com.shield.module.accounting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vendor_payment")
public class VendorPaymentEntity extends TenantAwareEntity {

    @Column(name = "vendor_id", nullable = false, columnDefinition = "uuid")
    private UUID vendorId;

    @Column(name = "expense_id", columnDefinition = "uuid")
    private UUID expenseId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VendorPaymentStatus status;
}
