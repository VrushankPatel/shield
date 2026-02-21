package com.shield.module.document.entity;

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
@Table(name = "document_access_log")
public class DocumentAccessLogEntity extends TenantAwareEntity {

    @Column(name = "document_id", nullable = false, columnDefinition = "uuid")
    private UUID documentId;

    @Column(name = "accessed_by", columnDefinition = "uuid")
    private UUID accessedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 50)
    private DocumentAccessType accessType;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;
}
