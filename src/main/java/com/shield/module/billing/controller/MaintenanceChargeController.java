package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.MaintenanceChargeGenerateRequest;
import com.shield.module.billing.dto.MaintenanceChargeResponse;
import com.shield.module.billing.dto.MaintenanceChargeUpdateRequest;
import com.shield.module.billing.service.BillingManagementService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/maintenance-charges")
@RequiredArgsConstructor
public class MaintenanceChargeController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceChargeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charges fetched", billingManagementService.listMaintenanceCharges()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceChargeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charge fetched", billingManagementService.getMaintenanceCharge(id)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MaintenanceChargeResponse>> generate(@Valid @RequestBody MaintenanceChargeGenerateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charge generated", billingManagementService.generateMaintenanceCharge(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MaintenanceChargeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceChargeUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charge updated", billingManagementService.updateMaintenanceCharge(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        billingManagementService.deleteMaintenanceCharge(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charge deleted", null));
    }

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<ApiResponse<List<MaintenanceChargeResponse>>> byCycle(@PathVariable UUID cycleId) {
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charges by cycle fetched", billingManagementService.listMaintenanceChargesByCycle(cycleId)));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<List<MaintenanceChargeResponse>>> byUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(ApiResponse.ok("Maintenance charges by unit fetched", billingManagementService.listMaintenanceChargesByUnit(unitId)));
    }
}
