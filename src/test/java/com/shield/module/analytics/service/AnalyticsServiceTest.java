package com.shield.module.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.analytics.dto.CollectionEfficiencyResponse;
import com.shield.module.analytics.dto.ReportTemplateCreateRequest;
import com.shield.module.analytics.dto.ReportTemplateResponse;
import com.shield.module.analytics.entity.AnalyticsDashboardEntity;
import com.shield.module.analytics.entity.ReportTemplateEntity;
import com.shield.module.analytics.repository.AnalyticsDashboardRepository;
import com.shield.module.analytics.repository.ReportTemplateRepository;
import com.shield.module.analytics.repository.ScheduledReportRepository;
import com.shield.security.model.ShieldPrincipal;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ReportTemplateRepository reportTemplateRepository;

    @Mock
    private ScheduledReportRepository scheduledReportRepository;

    @Mock
    private AnalyticsDashboardRepository analyticsDashboardRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                reportTemplateRepository,
                scheduledReportRepository,
                analyticsDashboardRepository,
                auditLogService,
                jdbcTemplate);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createReportTemplateShouldNormalizeTypeAndSetCreator() {
        when(reportTemplateRepository.save(any(ReportTemplateEntity.class))).thenAnswer(invocation -> {
            ReportTemplateEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        ReportTemplateCreateRequest request = new ReportTemplateCreateRequest(
                "Collection KPI",
                "collection_efficiency",
                "Monthly collection snapshot",
                null,
                null,
                false);

        ReportTemplateResponse response = analyticsService.createReportTemplate(request, principal);

        assertEquals("COLLECTION_EFFICIENCY", response.reportType());
        assertEquals(principal.userId(), response.createdBy());
    }

    @Test
    void getCollectionEfficiencyShouldCalculatePercentage() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(jdbcTemplate.queryForObject(contains("maintenance_bill"), eq(BigDecimal.class), eq(tenantId)))
                .thenReturn(BigDecimal.valueOf(2500));
        when(jdbcTemplate.queryForObject(contains("payment"), eq(BigDecimal.class), eq(tenantId)))
                .thenReturn(BigDecimal.valueOf(1000));

        CollectionEfficiencyResponse response = analyticsService.getCollectionEfficiency();

        assertEquals(BigDecimal.valueOf(2500).setScale(2), response.billedAmount());
        assertEquals(BigDecimal.valueOf(1000).setScale(2), response.collectedAmount());
        assertEquals(BigDecimal.valueOf(40).setScale(2), response.collectionEfficiencyPercent());
    }

    @Test
    void setDefaultDashboardShouldUnsetExistingDefaults() {
        UUID dashboardId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AnalyticsDashboardEntity existingDefault = new AnalyticsDashboardEntity();
        existingDefault.setId(UUID.randomUUID());
        existingDefault.setTenantId(tenantId);
        existingDefault.setDashboardName("Old Default");
        existingDefault.setDashboardType("COMMITTEE");
        existingDefault.setDefaultDashboard(true);

        AnalyticsDashboardEntity target = new AnalyticsDashboardEntity();
        target.setId(dashboardId);
        target.setTenantId(tenantId);
        target.setDashboardName("Operations Dashboard");
        target.setDashboardType("COMMITTEE");
        target.setDefaultDashboard(false);

        when(analyticsDashboardRepository.findAllByDefaultDashboardTrueAndDeletedFalse()).thenReturn(List.of(existingDefault));
        when(analyticsDashboardRepository.findByIdAndDeletedFalse(dashboardId)).thenReturn(Optional.of(target));
        when(analyticsDashboardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analyticsDashboardRepository.save(any(AnalyticsDashboardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        var response = analyticsService.setDefaultDashboard(dashboardId, principal);

        assertTrue(response.defaultDashboard());
        assertFalse(existingDefault.isDefaultDashboard());
    }
}
