package com.shield.module.announcement.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "announcement_read_receipt")
public class AnnouncementReadReceiptEntity extends TenantAwareEntity {

    @Column(name = "announcement_id", nullable = false, columnDefinition = "uuid")
    private UUID announcementId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;
}
