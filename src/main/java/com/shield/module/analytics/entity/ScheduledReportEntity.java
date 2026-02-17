package com.shield.module.analytics.entity;

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
@Table(name = "scheduled_report")
public class ScheduledReportEntity extends TenantAwareEntity {

    @Column(name = "template_id", nullable = false, columnDefinition = "uuid")
    private UUID templateId;

    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @Column(nullable = false, length = 50)
    private String frequency;

    @Column(length = 2000)
    private String recipients;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_generated_at")
    private Instant lastGeneratedAt;

    @Column(name = "next_generation_at")
    private Instant nextGenerationAt;
}
