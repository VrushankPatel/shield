package com.shield.module.poll.entity;

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
@Table(name = "poll")
public class PollEntity extends TenantAwareEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PollStatus status;

    @Column(name = "multiple_choice", nullable = false)
    private boolean multipleChoice;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;
}
