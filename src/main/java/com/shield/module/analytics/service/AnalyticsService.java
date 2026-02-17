package com.shield.module.analytics.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.analytics.dto.AmenityUtilizationResponse;
import com.shield.module.analytics.dto.AnalyticsDashboardCreateRequest;
import com.shield.module.analytics.dto.AnalyticsDashboardResponse;
import com.shield.module.analytics.dto.AnalyticsDashboardUpdateRequest;
import com.shield.module.analytics.dto.AssetFailureFrequencyResponse;
import com.shield.module.analytics.dto.CollectionEfficiencyResponse;
import com.shield.module.analytics.dto.ComplaintResolutionTimeResponse;
import com.shield.module.analytics.dto.DefaulterTrendResponse;
import com.shield.module.analytics.dto.ExpenseDistributionResponse;
import com.shield.module.analytics.dto.FundAllocationResponse;
import com.shield.module.analytics.dto.OccupancyRateResponse;
import com.shield.module.analytics.dto.ReportExecutionResponse;
import com.shield.module.analytics.dto.ReportTemplateCreateRequest;
import com.shield.module.analytics.dto.ReportTemplateResponse;
import com.shield.module.analytics.dto.ReportTemplateUpdateRequest;
import com.shield.module.analytics.dto.ScheduledReportCreateRequest;
import com.shield.module.analytics.dto.ScheduledReportResponse;
import com.shield.module.analytics.dto.ScheduledReportUpdateRequest;
import com.shield.module.analytics.dto.StaffAttendanceSummaryResponse;
import com.shield.module.analytics.dto.VisitorTrendResponse;
import com.shield.module.analytics.entity.AnalyticsDashboardEntity;
import com.shield.module.analytics.entity.ReportTemplateEntity;
import com.shield.module.analytics.entity.ScheduledReportEntity;
import com.shield.module.analytics.repository.AnalyticsDashboardRepository;
import com.shield.module.analytics.repository.ReportTemplateRepository;
import com.shield.module.analytics.repository.ScheduledReportRepository;
import com.shield.security.model.ShieldPrincipal;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalyticsService {

    private final ReportTemplateRepository reportTemplateRepository;
    private final ScheduledReportRepository scheduledReportRepository;
    private final AnalyticsDashboardRepository analyticsDashboardRepository;
    private final AuditLogService auditLogService;
    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(
            ReportTemplateRepository reportTemplateRepository,
            ScheduledReportRepository scheduledReportRepository,
            AnalyticsDashboardRepository analyticsDashboardRepository,
            AuditLogService auditLogService,
            JdbcTemplate jdbcTemplate) {
        this.reportTemplateRepository = reportTemplateRepository;
        this.scheduledReportRepository = scheduledReportRepository;
        this.analyticsDashboardRepository = analyticsDashboardRepository;
        this.auditLogService = auditLogService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReportTemplateResponse createReportTemplate(ReportTemplateCreateRequest request, ShieldPrincipal principal) {
        ReportTemplateEntity entity = new ReportTemplateEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTemplateName(request.templateName());
        entity.setReportType(normalizeType(request.reportType()));
        entity.setDescription(request.description());
        entity.setQueryTemplate(request.queryTemplate());
        entity.setParametersJson(request.parametersJson());
        entity.setCreatedBy(principal.userId());
        entity.setSystemTemplate(request.systemTemplate());

        ReportTemplateEntity saved = reportTemplateRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "REPORT_TEMPLATE_CREATED", "report_template", saved.getId(), null);
        return toReportTemplateResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReportTemplateResponse> listReportTemplates(Pageable pageable) {
        return PagedResponse.from(reportTemplateRepository.findAllByDeletedFalse(pageable).map(this::toReportTemplateResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReportTemplateResponse> listReportTemplatesByType(String type, Pageable pageable) {
        return PagedResponse.from(reportTemplateRepository.findAllByReportTypeAndDeletedFalse(normalizeType(type), pageable)
                .map(this::toReportTemplateResponse));
    }

    @Transactional(readOnly = true)
    public ReportTemplateResponse getReportTemplate(UUID id) {
        ReportTemplateEntity entity = reportTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found: " + id));
        return toReportTemplateResponse(entity);
    }

    public ReportTemplateResponse updateReportTemplate(UUID id, ReportTemplateUpdateRequest request, ShieldPrincipal principal) {
        ReportTemplateEntity entity = reportTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found: " + id));

        entity.setTemplateName(request.templateName());
        entity.setReportType(normalizeType(request.reportType()));
        entity.setDescription(request.description());
        entity.setQueryTemplate(request.queryTemplate());
        entity.setParametersJson(request.parametersJson());
        entity.setSystemTemplate(request.systemTemplate());

        ReportTemplateEntity saved = reportTemplateRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "REPORT_TEMPLATE_UPDATED", "report_template", saved.getId(), null);
        return toReportTemplateResponse(saved);
    }

    public void deleteReportTemplate(UUID id, ShieldPrincipal principal) {
        ReportTemplateEntity entity = reportTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found: " + id));

        entity.setDeleted(true);
        reportTemplateRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "REPORT_TEMPLATE_DELETED", "report_template", entity.getId(), null);
    }

    public ReportExecutionResponse executeReport(UUID id, ShieldPrincipal principal) {
        ReportTemplateEntity template = reportTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found: " + id));

        String reportType = normalizeType(template.getReportType());
        Map<String, Object> data = switch (reportType) {
            case "COLLECTION_EFFICIENCY" -> Map.of("collectionEfficiency", getCollectionEfficiency());
            case "EXPENSE_DISTRIBUTION" -> Map.of("expenseDistribution", getExpenseDistribution());
            case "COMPLAINT_RESOLUTION_TIME" -> Map.of("complaintResolutionTime", getComplaintResolutionTime());
            case "ASSET_FAILURE_FREQUENCY" -> Map.of("assetFailureFrequency", getAssetFailureFrequency());
            case "OCCUPANCY_RATE" -> Map.of("occupancyRate", getOccupancyRate());
            case "AMENITY_UTILIZATION" -> Map.of("amenityUtilization", getAmenityUtilization());
            case "DEFAULTER_TREND" -> Map.of("defaulterTrend", getDefaulterTrend());
            case "FUND_ALLOCATION" -> Map.of("fundAllocation", getFundAllocation());
            case "STAFF_ATTENDANCE_SUMMARY" -> Map.of("staffAttendanceSummary", getStaffAttendanceSummary());
            case "VISITOR_TRENDS" -> Map.of("visitorTrends", getVisitorTrends());
            default -> defaultExecutionPayload(template);
        };

        auditLogService.record(principal.tenantId(), principal.userId(), "REPORT_TEMPLATE_EXECUTED", "report_template", template.getId(), null);

        return new ReportExecutionResponse(
                template.getId(),
                template.getTemplateName(),
                reportType,
                Instant.now(),
                data);
    }

    public ScheduledReportResponse createScheduledReport(ScheduledReportCreateRequest request, ShieldPrincipal principal) {
        reportTemplateRepository.findByIdAndDeletedFalse(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Report template not found: " + request.templateId()));

        String frequency = normalizeFrequency(request.frequency());
        Instant nextGenerationAt = request.nextGenerationAt();
        if (nextGenerationAt == null) {
            nextGenerationAt = calculateNextGenerationAt(frequency, Instant.now());
        }

        ScheduledReportEntity entity = new ScheduledReportEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTemplateId(request.templateId());
        entity.setReportName(request.reportName());
        entity.setFrequency(frequency);
        entity.setRecipients(request.recipients());
        entity.setActive(request.active() == null || request.active());
        entity.setNextGenerationAt(nextGenerationAt);

        ScheduledReportEntity saved = scheduledReportRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SCHEDULED_REPORT_CREATED", "scheduled_report", saved.getId(), null);
        return toScheduledReportResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ScheduledReportResponse> listScheduledReports(Pageable pageable) {
        return PagedResponse.from(scheduledReportRepository.findAllByDeletedFalse(pageable).map(this::toScheduledReportResponse));
    }

    @Transactional(readOnly = true)
    public ScheduledReportResponse getScheduledReport(UUID id) {
        ScheduledReportEntity entity = scheduledReportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found: " + id));
        return toScheduledReportResponse(entity);
    }

    public ScheduledReportResponse updateScheduledReport(UUID id, ScheduledReportUpdateRequest request, ShieldPrincipal principal) {
        ScheduledReportEntity entity = scheduledReportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found: " + id));

        String frequency = normalizeFrequency(request.frequency());
        entity.setReportName(request.reportName());
        entity.setFrequency(frequency);
        entity.setRecipients(request.recipients());
        entity.setNextGenerationAt(request.nextGenerationAt() == null
                ? calculateNextGenerationAt(frequency, Instant.now())
                : request.nextGenerationAt());

        ScheduledReportEntity saved = scheduledReportRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SCHEDULED_REPORT_UPDATED", "scheduled_report", saved.getId(), null);
        return toScheduledReportResponse(saved);
    }

    public void deleteScheduledReport(UUID id, ShieldPrincipal principal) {
        ScheduledReportEntity entity = scheduledReportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found: " + id));

        entity.setDeleted(true);
        scheduledReportRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SCHEDULED_REPORT_DELETED", "scheduled_report", entity.getId(), null);
    }

    public ScheduledReportResponse activateScheduledReport(UUID id, ShieldPrincipal principal) {
        return updateScheduledReportStatus(id, true, principal, "SCHEDULED_REPORT_ACTIVATED");
    }

    public ScheduledReportResponse deactivateScheduledReport(UUID id, ShieldPrincipal principal) {
        return updateScheduledReportStatus(id, false, principal, "SCHEDULED_REPORT_DEACTIVATED");
    }

    public ScheduledReportResponse sendScheduledReportNow(UUID id, ShieldPrincipal principal) {
        ScheduledReportEntity entity = scheduledReportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found: " + id));

        Instant now = Instant.now();
        entity.setLastGeneratedAt(now);
        entity.setNextGenerationAt(calculateNextGenerationAt(entity.getFrequency(), now));

        ScheduledReportEntity saved = scheduledReportRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SCHEDULED_REPORT_SENT_NOW", "scheduled_report", saved.getId(), null);
        return toScheduledReportResponse(saved);
    }

    public AnalyticsDashboardResponse createDashboard(AnalyticsDashboardCreateRequest request, ShieldPrincipal principal) {
        if (request.defaultDashboard()) {
            clearDefaultDashboards();
        }

        AnalyticsDashboardEntity entity = new AnalyticsDashboardEntity();
        entity.setTenantId(principal.tenantId());
        entity.setDashboardName(request.dashboardName());
        entity.setDashboardType(normalizeType(request.dashboardType()));
        entity.setWidgetsJson(request.widgetsJson());
        entity.setCreatedBy(principal.userId());
        entity.setDefaultDashboard(request.defaultDashboard());

        AnalyticsDashboardEntity saved = analyticsDashboardRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ANALYTICS_DASHBOARD_CREATED", "analytics_dashboard", saved.getId(), null);
        return toDashboardResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnalyticsDashboardResponse> listDashboards(Pageable pageable) {
        return PagedResponse.from(analyticsDashboardRepository.findAllByDeletedFalse(pageable).map(this::toDashboardResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnalyticsDashboardResponse> listDashboardsByType(String type, Pageable pageable) {
        return PagedResponse.from(analyticsDashboardRepository.findAllByDashboardTypeAndDeletedFalse(normalizeType(type), pageable)
                .map(this::toDashboardResponse));
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getDashboard(UUID id) {
        AnalyticsDashboardEntity entity = analyticsDashboardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics dashboard not found: " + id));
        return toDashboardResponse(entity);
    }

    public AnalyticsDashboardResponse updateDashboard(UUID id, AnalyticsDashboardUpdateRequest request, ShieldPrincipal principal) {
        AnalyticsDashboardEntity entity = analyticsDashboardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics dashboard not found: " + id));

        if (request.defaultDashboard()) {
            clearDefaultDashboards();
        }

        entity.setDashboardName(request.dashboardName());
        entity.setDashboardType(normalizeType(request.dashboardType()));
        entity.setWidgetsJson(request.widgetsJson());
        entity.setDefaultDashboard(request.defaultDashboard());

        AnalyticsDashboardEntity saved = analyticsDashboardRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ANALYTICS_DASHBOARD_UPDATED", "analytics_dashboard", saved.getId(), null);
        return toDashboardResponse(saved);
    }

    public void deleteDashboard(UUID id, ShieldPrincipal principal) {
        AnalyticsDashboardEntity entity = analyticsDashboardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics dashboard not found: " + id));

        entity.setDeleted(true);
        analyticsDashboardRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ANALYTICS_DASHBOARD_DELETED", "analytics_dashboard", entity.getId(), null);
    }

    public AnalyticsDashboardResponse setDefaultDashboard(UUID id, ShieldPrincipal principal) {
        AnalyticsDashboardEntity entity = analyticsDashboardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics dashboard not found: " + id));

        clearDefaultDashboards();
        entity.setDefaultDashboard(true);

        AnalyticsDashboardEntity saved = analyticsDashboardRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ANALYTICS_DASHBOARD_SET_DEFAULT", "analytics_dashboard", saved.getId(), null);
        return toDashboardResponse(saved);
    }

    @Transactional(readOnly = true)
    public CollectionEfficiencyResponse getCollectionEfficiency() {
        UUID tenantId = TenantContext.getRequiredTenantId();

        BigDecimal billedAmount = queryBigDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM maintenance_bill WHERE tenant_id = ? AND deleted = FALSE",
                tenantId);
        BigDecimal collectedAmount = queryBigDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM payment WHERE tenant_id = ? AND deleted = FALSE",
                tenantId);

        return new CollectionEfficiencyResponse(
                billedAmount,
                collectedAmount,
                calculatePercentage(collectedAmount, billedAmount));
    }

    @Transactional(readOnly = true)
    public List<ExpenseDistributionResponse> getExpenseDistribution() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT category, COALESCE(SUM(amount), 0) AS amount
                FROM ledger_entry
                WHERE tenant_id = ? AND deleted = FALSE AND type = 'EXPENSE'
                GROUP BY category
                ORDER BY amount DESC
                """,
                (rs, rowNum) -> new ExpenseDistributionResponse(
                        rs.getString("category"),
                        scale(rs.getBigDecimal("amount"))),
                tenantId);
    }

    @Transactional(readOnly = true)
    public ComplaintResolutionTimeResponse getComplaintResolutionTime() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT
                    COUNT(*) AS resolved_count,
                    COALESCE(AVG(EXTRACT(EPOCH FROM (resolved_at - created_at)) / 3600), 0) AS avg_hours
                FROM complaint
                WHERE tenant_id = ? AND deleted = FALSE AND resolved_at IS NOT NULL
                """,
                tenantId);

        long resolvedCount = ((Number) row.get("resolved_count")).longValue();
        BigDecimal avgHours = scale(new BigDecimal(String.valueOf(row.get("avg_hours"))));
        return new ComplaintResolutionTimeResponse(resolvedCount, avgHours);
    }

    @Transactional(readOnly = true)
    public List<AssetFailureFrequencyResponse> getAssetFailureFrequency() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT COALESCE(a.asset_code, 'UNMAPPED') AS asset_code, COUNT(*) AS complaint_count
                FROM complaint c
                LEFT JOIN asset a ON a.id = c.asset_id AND a.deleted = FALSE
                WHERE c.tenant_id = ? AND c.deleted = FALSE
                GROUP BY COALESCE(a.asset_code, 'UNMAPPED')
                ORDER BY complaint_count DESC
                LIMIT 10
                """,
                (rs, rowNum) -> new AssetFailureFrequencyResponse(
                        rs.getString("asset_code"),
                        rs.getLong("complaint_count")),
                tenantId);
    }

    @Transactional(readOnly = true)
    public OccupancyRateResponse getOccupancyRate() {
        UUID tenantId = TenantContext.getRequiredTenantId();

        long totalUnits = queryLong(
                "SELECT COUNT(*) FROM unit WHERE tenant_id = ? AND deleted = FALSE",
                tenantId);
        long occupiedUnits = queryLong(
                """
                SELECT COUNT(DISTINCT unit_id)
                FROM users
                WHERE tenant_id = ?
                  AND deleted = FALSE
                  AND status = 'ACTIVE'
                  AND unit_id IS NOT NULL
                """,
                tenantId);

        return new OccupancyRateResponse(totalUnits, occupiedUnits, calculatePercentage(occupiedUnits, totalUnits));
    }

    @Transactional(readOnly = true)
    public List<AmenityUtilizationResponse> getAmenityUtilization() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT a.id, a.name, COUNT(b.id) AS booking_count
                FROM amenity a
                LEFT JOIN amenity_booking b
                  ON b.amenity_id = a.id
                 AND b.deleted = FALSE
                 AND b.tenant_id = ?
                WHERE a.tenant_id = ? AND a.deleted = FALSE
                GROUP BY a.id, a.name
                ORDER BY booking_count DESC
                """,
                (rs, rowNum) -> new AmenityUtilizationResponse(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getLong("booking_count")),
                tenantId,
                tenantId);
    }

    @Transactional(readOnly = true)
    public List<DefaulterTrendResponse> getDefaulterTrend() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT
                    CAST(year AS text) || '-' || LPAD(CAST(month AS text), 2, '0') AS period,
                    COUNT(DISTINCT unit_id) AS defaulter_units,
                    COALESCE(SUM(amount), 0) AS outstanding_amount
                FROM maintenance_bill
                WHERE tenant_id = ?
                  AND deleted = FALSE
                  AND status IN ('PENDING', 'OVERDUE')
                GROUP BY year, month
                ORDER BY year DESC, month DESC
                LIMIT 6
                """,
                (rs, rowNum) -> new DefaulterTrendResponse(
                        rs.getString("period"),
                        rs.getLong("defaulter_units"),
                        scale(rs.getBigDecimal("outstanding_amount"))),
                tenantId);
    }

    @Transactional(readOnly = true)
    public List<FundAllocationResponse> getFundAllocation() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT category, COALESCE(SUM(amount), 0) AS amount
                FROM ledger_entry
                WHERE tenant_id = ? AND deleted = FALSE
                GROUP BY category
                ORDER BY amount DESC
                LIMIT 10
                """,
                (rs, rowNum) -> new FundAllocationResponse(
                        rs.getString("category"),
                        scale(rs.getBigDecimal("amount"))),
                tenantId);
    }

    @Transactional(readOnly = true)
    public StaffAttendanceSummaryResponse getStaffAttendanceSummary() {
        UUID tenantId = TenantContext.getRequiredTenantId();

        long totalStaff = queryLong(
                "SELECT COUNT(*) FROM staff WHERE tenant_id = ? AND deleted = FALSE AND active = TRUE",
                tenantId);
        long presentToday = queryLong(
                """
                SELECT COUNT(DISTINCT staff_id)
                FROM staff_attendance
                WHERE tenant_id = ?
                  AND deleted = FALSE
                  AND attendance_date = CURRENT_DATE
                  AND status = 'PRESENT'
                """,
                tenantId);

        long absentToday = Math.max(totalStaff - presentToday, 0);
        return new StaffAttendanceSummaryResponse(totalStaff, presentToday, absentToday);
    }

    @Transactional(readOnly = true)
    public List<VisitorTrendResponse> getVisitorTrends() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return jdbcTemplate.query(
                """
                SELECT DATE(valid_from) AS visit_date, COUNT(*) AS visitor_count
                FROM visitor_pass
                WHERE tenant_id = ? AND deleted = FALSE
                GROUP BY DATE(valid_from)
                ORDER BY visit_date DESC
                LIMIT 7
                """,
                (rs, rowNum) -> {
                    Date visitDate = rs.getDate("visit_date");
                    return new VisitorTrendResponse(
                            visitDate == null ? LocalDate.now() : visitDate.toLocalDate(),
                            rs.getLong("visitor_count"));
                },
                tenantId);
    }

    private ScheduledReportResponse updateScheduledReportStatus(
            UUID id,
            boolean active,
            ShieldPrincipal principal,
            String action) {

        ScheduledReportEntity entity = scheduledReportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found: " + id));

        entity.setActive(active);
        ScheduledReportEntity saved = scheduledReportRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), action, "scheduled_report", saved.getId(), null);
        return toScheduledReportResponse(saved);
    }

    private void clearDefaultDashboards() {
        List<AnalyticsDashboardEntity> existingDefaults = analyticsDashboardRepository.findAllByDefaultDashboardTrueAndDeletedFalse();
        for (AnalyticsDashboardEntity dashboard : existingDefaults) {
            dashboard.setDefaultDashboard(false);
        }
        analyticsDashboardRepository.saveAll(existingDefaults);
    }

    private Map<String, Object> defaultExecutionPayload(ReportTemplateEntity template) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "No built-in execution mapping for this report type");
        payload.put("queryTemplate", template.getQueryTemplate());
        payload.put("parametersJson", template.getParametersJson());
        return payload;
    }

    private String normalizeType(String type) {
        return type == null ? "UNKNOWN" : type.trim().toUpperCase();
    }

    private String normalizeFrequency(String frequency) {
        return frequency == null ? "WEEKLY" : frequency.trim().toUpperCase();
    }

    private Instant calculateNextGenerationAt(String frequency, Instant from) {
        return switch (normalizeFrequency(frequency)) {
            case "DAILY" -> from.plus(1, ChronoUnit.DAYS);
            case "WEEKLY" -> from.plus(7, ChronoUnit.DAYS);
            case "MONTHLY" -> from.plus(30, ChronoUnit.DAYS);
            case "QUARTERLY" -> from.plus(90, ChronoUnit.DAYS);
            default -> from.plus(7, ChronoUnit.DAYS);
        };
    }

    private BigDecimal calculatePercentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerator
                .multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private long queryLong(String sql, Object... params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params);
        return value == null ? 0L : value;
    }

    private BigDecimal queryBigDecimal(String sql, Object... params) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, params);
        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private ReportTemplateResponse toReportTemplateResponse(ReportTemplateEntity entity) {
        return new ReportTemplateResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTemplateName(),
                entity.getReportType(),
                entity.getDescription(),
                entity.getQueryTemplate(),
                entity.getParametersJson(),
                entity.getCreatedBy(),
                entity.isSystemTemplate(),
                entity.getCreatedAt());
    }

    private ScheduledReportResponse toScheduledReportResponse(ScheduledReportEntity entity) {
        return new ScheduledReportResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTemplateId(),
                entity.getReportName(),
                entity.getFrequency(),
                entity.getRecipients(),
                entity.isActive(),
                entity.getLastGeneratedAt(),
                entity.getNextGenerationAt());
    }

    private AnalyticsDashboardResponse toDashboardResponse(AnalyticsDashboardEntity entity) {
        return new AnalyticsDashboardResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getDashboardName(),
                entity.getDashboardType(),
                entity.getWidgetsJson(),
                entity.getCreatedBy(),
                entity.isDefaultDashboard());
    }
}
