package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.ExpenseCreateRequest;
import com.shield.module.accounting.dto.ExpenseResponse;
import com.shield.module.accounting.dto.ExpenseUpdateRequest;
import com.shield.module.accounting.service.AccountingTreasuryService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Expenses fetched", accountingTreasuryService.listExpenses(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Expense fetched", accountingTreasuryService.getExpense(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@Valid @RequestBody ExpenseCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Expense created", accountingTreasuryService.createExpense(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(@PathVariable UUID id, @Valid @RequestBody ExpenseUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Expense updated", accountingTreasuryService.updateExpense(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteExpense(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted", null));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> approve(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Expense approved", accountingTreasuryService.approveExpense(id, principal)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> reject(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Expense rejected", accountingTreasuryService.rejectExpense(id, principal)));
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> pending(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending expenses fetched", accountingTreasuryService.listPendingExpenses(pageable)));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> byVendor(@PathVariable UUID vendorId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Expenses by vendor fetched", accountingTreasuryService.listExpensesByVendor(vendorId, pageable)));
    }

    @GetMapping("/account/{accountHeadId}")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> byAccount(@PathVariable UUID accountHeadId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Expenses by account head fetched", accountingTreasuryService.listExpensesByAccountHead(accountHeadId, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Expenses by date range fetched", accountingTreasuryService.listExpensesByDateRange(fromDate, toDate, pageable)));
    }

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<String>> export() {
        return ResponseEntity.ok(ApiResponse.ok("Expense export generated", accountingTreasuryService.exportExpenses()));
    }
}
