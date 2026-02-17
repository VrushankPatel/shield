package com.shield.module.analytics.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_template")
public class ReportTemplateEntity extends TenantAwareEntity {

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType;

    @Column(length = 1000)
    private String description;

    @Column(name = "query_template", columnDefinition = "text")
    private String queryTemplate;

    @Column(name = "parameters_json", columnDefinition = "text")
    private String parametersJson;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "system_template", nullable = false)
    private boolean systemTemplate;
}
