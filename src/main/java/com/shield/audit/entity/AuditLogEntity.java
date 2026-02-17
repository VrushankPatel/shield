package com.shield.audit.entity;

import com.shield.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLogEntity extends BaseEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 128)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 128)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;
}
