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
@Table(name = "ledger_entry")
public class LedgerEntryEntity extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerType type;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 128)
    private String reference;

    @Column(length = 1000)
    private String description;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "account_head_id", columnDefinition = "uuid")
    private UUID accountHeadId;

    @Column(name = "fund_category_id", columnDefinition = "uuid")
    private UUID fundCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 50)
    private LedgerTransactionType transactionType;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", columnDefinition = "uuid")
    private UUID referenceId;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;
}
