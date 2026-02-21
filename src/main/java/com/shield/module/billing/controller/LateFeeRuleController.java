package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.LateFeeRuleCreateRequest;
import com.shield.module.billing.dto.LateFeeRuleResponse;
import com.shield.module.billing.dto.LateFeeRuleUpdateRequest;
import com.shield.module.billing.service.BillingManagementService;
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
@RequestMapping("/api/v1/late-fee-rules")
@RequiredArgsConstructor
public class LateFeeRuleController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LateFeeRuleResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Late fee rules fetched", billingManagementService.listLateFeeRules(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LateFeeRuleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule fetched", billingManagementService.getLateFeeRule(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LateFeeRuleResponse>> create(@Valid @RequestBody LateFeeRuleCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule created", billingManagementService.createLateFeeRule(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LateFeeRuleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LateFeeRuleUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule updated", billingManagementService.updateLateFeeRule(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        billingManagementService.deleteLateFeeRule(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule deleted", null));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LateFeeRuleResponse>> activate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule activated", billingManagementService.activateLateFeeRule(id, principal)));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LateFeeRuleResponse>> deactivate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Late fee rule deactivated", billingManagementService.deactivateLateFeeRule(id, principal)));
    }
}
