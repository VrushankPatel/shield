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
@Table(name = "analytics_dashboard")
public class AnalyticsDashboardEntity extends TenantAwareEntity {

    @Column(name = "dashboard_name", nullable = false, length = 255)
    private String dashboardName;

    @Column(name = "dashboard_type", nullable = false, length = 100)
    private String dashboardType;

    @Column(name = "widgets_json", columnDefinition = "text")
    private String widgetsJson;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "default_dashboard", nullable = false)
    private boolean defaultDashboard;
}
