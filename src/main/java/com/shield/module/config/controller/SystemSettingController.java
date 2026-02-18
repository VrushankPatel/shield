package com.shield.module.config.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.config.dto.JsonSettingResponse;
import com.shield.module.config.dto.JsonSettingUpdateRequest;
import com.shield.module.config.dto.ModuleSettingResponse;
import com.shield.module.config.dto.ModuleToggleRequest;
import com.shield.module.config.service.SystemSettingService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/settings")
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping("/modules")
    public ResponseEntity<ApiResponse<List<ModuleSettingResponse>>> listModules() {
        return ResponseEntity.ok(ApiResponse.ok("Module settings fetched", systemSettingService.listModules()));
    }

    @PutMapping("/modules/{module}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModuleSettingResponse>> toggleModule(
            @PathVariable @Size(max = 80) String module,
            @Valid @RequestBody ModuleToggleRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                "Module setting updated",
                systemSettingService.toggleModule(module, request, principal)));
    }

    @GetMapping("/billing-formula")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> getBillingFormula() {
        return ResponseEntity.ok(ApiResponse.ok("Billing formula fetched", systemSettingService.getBillingFormula()));
    }

    @PutMapping("/billing-formula")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> updateBillingFormula(
            @Valid @RequestBody JsonSettingUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                "Billing formula updated",
                systemSettingService.updateBillingFormula(request, principal)));
    }

    @GetMapping("/sla-rules")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> getSlaRules() {
        return ResponseEntity.ok(ApiResponse.ok("SLA rules fetched", systemSettingService.getSlaRules()));
    }

    @PutMapping("/sla-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> updateSlaRules(
            @Valid @RequestBody JsonSettingUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                "SLA rules updated",
                systemSettingService.updateSlaRules(request, principal)));
    }

    @GetMapping("/notification-channels")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> getNotificationChannels() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Notification channels fetched",
                systemSettingService.getNotificationChannels()));
    }

    @PutMapping("/notification-channels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JsonSettingResponse>> updateNotificationChannels(
            @Valid @RequestBody JsonSettingUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                "Notification channels updated",
                systemSettingService.updateNotificationChannels(request, principal)));
    }
}
