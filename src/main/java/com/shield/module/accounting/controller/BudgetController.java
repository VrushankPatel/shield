package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.BudgetCreateRequest;
import com.shield.module.accounting.dto.BudgetResponse;
import com.shield.module.accounting.dto.BudgetUpdateRequest;
import com.shield.module.accounting.dto.BudgetVsActualResponse;
import com.shield.module.accounting.service.AccountingTreasuryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BudgetResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Budgets fetched", accountingTreasuryService.listBudgets(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Budget fetched", accountingTreasuryService.getBudget(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BudgetResponse>> create(@Valid @RequestBody BudgetCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Budget created", accountingTreasuryService.createBudget(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(@PathVariable UUID id, @Valid @RequestBody BudgetUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Budget updated", accountingTreasuryService.updateBudget(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteBudget(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Budget deleted", null));
    }

    @GetMapping("/financial-year/{year}")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> byYear(@PathVariable String year) {
        return ResponseEntity.ok(ApiResponse.ok("Budgets by year fetched", accountingTreasuryService.listBudgetsByFinancialYear(year)));
    }

    @GetMapping("/vs-actual")
    public ResponseEntity<ApiResponse<List<BudgetVsActualResponse>>> vsActual(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("Budget vs actual fetched", accountingTreasuryService.listBudgetVsActual(financialYear)));
    }
}
