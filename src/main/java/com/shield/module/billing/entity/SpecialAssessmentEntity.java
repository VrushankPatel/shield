package com.shield.module.billing.entity;

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
@Table(name = "special_assessment")
public class SpecialAssessmentEntity extends TenantAwareEntity {

    @Column(name = "assessment_name", nullable = false, length = 255)
    private String assessmentName;

    @Column(length = 1000)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "per_unit_amount", precision = 12, scale = 2)
    private BigDecimal perUnitAmount;

    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SpecialAssessmentStatus status;
}
