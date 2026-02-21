package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.LedgerEntryBulkCreateRequest;
import com.shield.module.accounting.dto.LedgerEntryCreateRequest;
import com.shield.module.accounting.dto.LedgerEntryResponse;
import com.shield.module.accounting.dto.LedgerEntryUpdateRequest;
import com.shield.module.accounting.service.AccountingTreasuryService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger-entries")
@RequiredArgsConstructor
public class LedgerEntryController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LedgerEntryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries fetched", accountingTreasuryService.listLedgerEntries(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entry fetched", accountingTreasuryService.getLedgerEntry(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> create(@Valid @RequestBody LedgerEntryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Ledger entry created", accountingTreasuryService.createLedgerEntry(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody LedgerEntryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Ledger entry updated", accountingTreasuryService.updateLedgerEntry(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteLedgerEntry(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Ledger entry deleted", null));
    }

    @GetMapping("/account/{accountHeadId}")
    public ResponseEntity<ApiResponse<PagedResponse<LedgerEntryResponse>>> byAccount(@PathVariable UUID accountHeadId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries by account head fetched", accountingTreasuryService.listLedgerEntriesByAccountHead(accountHeadId, pageable)));
    }

    @GetMapping("/fund/{fundCategoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<LedgerEntryResponse>>> byFund(@PathVariable UUID fundCategoryId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries by fund fetched", accountingTreasuryService.listLedgerEntriesByFundCategory(fundCategoryId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<LedgerEntryResponse>>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries by date range fetched", accountingTreasuryService.listLedgerEntriesByDateRange(fromDate, toDate, pageable)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> bulk(@Valid @RequestBody LedgerEntryBulkCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries bulk created", accountingTreasuryService.bulkCreateLedgerEntries(request, principal)));
    }

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<String>> export() {
        return ResponseEntity.ok(ApiResponse.ok("Ledger export generated", accountingTreasuryService.exportLedgerEntries()));
    }
}
