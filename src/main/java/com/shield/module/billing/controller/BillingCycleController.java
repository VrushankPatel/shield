package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.BillingCycleCreateRequest;
import com.shield.module.billing.dto.BillingCycleResponse;
import com.shield.module.billing.dto.BillingCycleUpdateRequest;
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
@RequestMapping("/api/v1/billing-cycles")
@RequiredArgsConstructor
public class BillingCycleController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BillingCycleResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Billing cycles fetched", billingManagementService.listBillingCycles(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle fetched", billingManagementService.getBillingCycle(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> create(@Valid @RequestBody BillingCycleCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle created", billingManagementService.createBillingCycle(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BillingCycleUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle updated", billingManagementService.updateBillingCycle(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        billingManagementService.deleteBillingCycle(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle deleted", null));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> publish(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle published", billingManagementService.publishBillingCycle(id, principal)));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> close(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle closed", billingManagementService.closeBillingCycle(id, principal)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> current() {
        return ResponseEntity.ok(ApiResponse.ok("Current billing cycle fetched", billingManagementService.getCurrentBillingCycle()));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<PagedResponse<BillingCycleResponse>>> byYear(@PathVariable int year, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Billing cycles by year fetched", billingManagementService.listBillingCyclesByYear(year, pageable)));
    }
}
