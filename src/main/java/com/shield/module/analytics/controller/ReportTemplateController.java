package com.shield.module.analytics.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.analytics.dto.ReportExecutionResponse;
import com.shield.module.analytics.dto.ReportTemplateCreateRequest;
import com.shield.module.analytics.dto.ReportTemplateResponse;
import com.shield.module.analytics.dto.ReportTemplateUpdateRequest;
import com.shield.module.analytics.service.AnalyticsService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report-templates")
@RequiredArgsConstructor
public class ReportTemplateController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<ReportTemplateResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Report templates fetched", analyticsService.listReportTemplates(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ReportTemplateResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Report template fetched", analyticsService.getReportTemplate(id)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<ReportTemplateResponse>>> listByType(
            @PathVariable String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Report templates by type fetched",
                analyticsService.listReportTemplatesByType(type, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ReportTemplateResponse>> create(@Valid @RequestBody ReportTemplateCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Report template created", analyticsService.createReportTemplate(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ReportTemplateResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReportTemplateUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Report template updated", analyticsService.updateReportTemplate(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        analyticsService.deleteReportTemplate(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Report template deleted", null));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ReportExecutionResponse>> execute(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Report template executed", analyticsService.executeReport(id, principal)));
    }
}
