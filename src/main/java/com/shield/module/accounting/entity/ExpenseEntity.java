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
@Table(name = "expense")
public class ExpenseEntity extends TenantAwareEntity {

    @Column(name = "expense_number", nullable = false, unique = true, length = 100)
    private String expenseNumber;

    @Column(name = "account_head_id", nullable = false, columnDefinition = "uuid")
    private UUID accountHeadId;

    @Column(name = "fund_category_id", columnDefinition = "uuid")
    private UUID fundCategoryId;

    @Column(name = "vendor_id", columnDefinition = "uuid")
    private UUID vendorId;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 1000)
    private String description;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_url", length = 1000)
    private String invoiceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private ExpensePaymentStatus paymentStatus;

    @Column(name = "approved_by", columnDefinition = "uuid")
    private UUID approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}
