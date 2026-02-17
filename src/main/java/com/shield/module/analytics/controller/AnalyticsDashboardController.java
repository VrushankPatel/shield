package com.shield.module.analytics.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.analytics.dto.AnalyticsDashboardCreateRequest;
import com.shield.module.analytics.dto.AnalyticsDashboardResponse;
import com.shield.module.analytics.dto.AnalyticsDashboardUpdateRequest;
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
@RequestMapping("/api/v1/analytics-dashboards")
@RequiredArgsConstructor
public class AnalyticsDashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<AnalyticsDashboardResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboards fetched", analyticsService.listDashboards(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboard fetched", analyticsService.getDashboard(id)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<AnalyticsDashboardResponse>>> listByType(
            @PathVariable String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboards by type fetched",
                analyticsService.listDashboardsByType(type, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> create(
            @Valid @RequestBody AnalyticsDashboardCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboard created", analyticsService.createDashboard(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AnalyticsDashboardUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboard updated", analyticsService.updateDashboard(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        analyticsService.deleteDashboard(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboard deleted", null));
    }

    @PostMapping("/{id}/set-default")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> setDefault(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Analytics dashboard set as default", analyticsService.setDefaultDashboard(id, principal)));
    }
}
