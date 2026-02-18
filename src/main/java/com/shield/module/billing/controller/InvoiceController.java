package com.shield.module.billing.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.billing.dto.InvoiceBulkGenerateRequest;
import com.shield.module.billing.dto.InvoiceGenerateRequest;
import com.shield.module.billing.dto.InvoiceResponse;
import com.shield.module.billing.dto.InvoiceUpdateRequest;
import com.shield.module.billing.service.BillingManagementService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final BillingManagementService billingManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Invoices fetched", billingManagementService.listInvoices(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice fetched", billingManagementService.getInvoice(id)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generate(@Valid @RequestBody InvoiceGenerateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Invoice generated", billingManagementService.generateInvoice(request, principal)));
    }

    @PostMapping("/bulk-generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> bulkGenerate(@Valid @RequestBody InvoiceBulkGenerateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Invoices bulk generated", billingManagementService.bulkGenerateInvoices(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(@PathVariable UUID id, @Valid @RequestBody InvoiceUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Invoice updated", billingManagementService.updateInvoice(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        billingManagementService.deleteInvoice(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Invoice deleted", null));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> byUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(ApiResponse.ok("Invoices by unit fetched", billingManagementService.listInvoicesByUnit(unitId)));
    }

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> byCycle(@PathVariable UUID cycleId) {
        return ResponseEntity.ok(ApiResponse.ok("Invoices by cycle fetched", billingManagementService.listInvoicesByCycle(cycleId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> byStatus(@PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Invoices by status fetched", billingManagementService.listInvoicesByStatus(status, pageable)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<InvoiceResponse>> download(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Invoice download info fetched", billingManagementService.getInvoiceDownloadInfo(id)));
    }

    @GetMapping("/defaulters")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> defaulters(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Defaulters fetched", billingManagementService.listDefaulters(pageable)));
    }

    @GetMapping("/outstanding")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> outstanding(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Outstanding invoices fetched", billingManagementService.listOutstanding(pageable)));
    }
}
