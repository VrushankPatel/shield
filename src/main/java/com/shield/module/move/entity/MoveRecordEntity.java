package com.shield.module.move.entity;

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
@Table(name = "move_record")
public class MoveRecordEntity extends TenantAwareEntity {

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_type", nullable = false, length = 20)
    private MoveType moveType;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "security_deposit", precision = 12, scale = 2)
    private BigDecimal securityDeposit;

    @Column(name = "agreement_url", length = 2000)
    private String agreementUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MoveStatus status;

    @Column(name = "decision_notes", length = 500)
    private String decisionNotes;

    @Column(name = "approved_by", columnDefinition = "uuid")
    private UUID approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}
