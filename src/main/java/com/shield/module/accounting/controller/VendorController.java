package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.VendorCreateRequest;
import com.shield.module.accounting.dto.VendorResponse;
import com.shield.module.accounting.dto.VendorStatusUpdateRequest;
import com.shield.module.accounting.dto.VendorUpdateRequest;
import com.shield.module.accounting.service.AccountingTreasuryService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Vendors fetched", accountingTreasuryService.listVendors(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor fetched", accountingTreasuryService.getVendor(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<VendorResponse>> create(@Valid @RequestBody VendorCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Vendor created", accountingTreasuryService.createVendor(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<VendorResponse>> update(@PathVariable UUID id, @Valid @RequestBody VendorUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Vendor updated", accountingTreasuryService.updateVendor(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteVendor(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Vendor deleted", null));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> byType(@PathVariable String type, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Vendors by type fetched", accountingTreasuryService.listVendorsByType(type, pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> active(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active vendors fetched", accountingTreasuryService.listActiveVendors(pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<VendorResponse>> status(@PathVariable UUID id, @Valid @RequestBody VendorStatusUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Vendor status updated", accountingTreasuryService.updateVendorStatus(id, request.active(), principal)));
    }
}
