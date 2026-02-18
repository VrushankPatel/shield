package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.VendorPaymentCreateRequest;
import com.shield.module.accounting.dto.VendorPaymentResponse;
import com.shield.module.accounting.service.AccountingTreasuryService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vendor-payments")
@RequiredArgsConstructor
public class VendorPaymentController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VendorPaymentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor payments fetched", accountingTreasuryService.listVendorPayments(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorPaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor payment fetched", accountingTreasuryService.getVendorPayment(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<VendorPaymentResponse>> create(@Valid @RequestBody VendorPaymentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Vendor payment created", accountingTreasuryService.createVendorPayment(request, principal)));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<PagedResponse<VendorPaymentResponse>>> byVendor(@PathVariable UUID vendorId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor payments by vendor fetched", accountingTreasuryService.listVendorPaymentsByVendor(vendorId, pageable)));
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<ApiResponse<PagedResponse<VendorPaymentResponse>>> byExpense(@PathVariable UUID expenseId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor payments by expense fetched", accountingTreasuryService.listVendorPaymentsByExpense(expenseId, pageable)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PagedResponse<VendorPaymentResponse>>> pending(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending vendor payments fetched", accountingTreasuryService.listPendingVendorPayments(pageable)));
    }
}
