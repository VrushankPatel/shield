package com.shield.module.unit.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "unit_ownership_history")
public class UnitOwnershipHistoryEntity extends TenantAwareEntity {

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_ownership_status", nullable = false, length = 20)
    private UnitOwnershipStatus previousOwnershipStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_ownership_status", nullable = false, length = 20)
    private UnitOwnershipStatus newOwnershipStatus;

    @Column(name = "changed_by", columnDefinition = "uuid")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(length = 500)
    private String notes;
}
