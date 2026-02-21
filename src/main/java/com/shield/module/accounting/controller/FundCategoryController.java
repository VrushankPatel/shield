package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.FundBalanceResponse;
import com.shield.module.accounting.dto.FundCategoryCreateRequest;
import com.shield.module.accounting.dto.FundCategoryResponse;
import com.shield.module.accounting.dto.FundCategoryUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fund-categories")
@RequiredArgsConstructor
public class FundCategoryController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FundCategoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Fund categories fetched", accountingTreasuryService.listFundCategories(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FundCategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Fund category fetched", accountingTreasuryService.getFundCategory(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<FundCategoryResponse>> create(@Valid @RequestBody FundCategoryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Fund category created", accountingTreasuryService.createFundCategory(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<FundCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FundCategoryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Fund category updated", accountingTreasuryService.updateFundCategory(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteFundCategory(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Fund category deleted", null));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<FundBalanceResponse>> balance(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Fund balance fetched", accountingTreasuryService.getFundBalance(id)));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<FundBalanceResponse>>> balances() {
        return ResponseEntity.ok(ApiResponse.ok("Fund balances fetched", accountingTreasuryService.listFundBalances()));
    }
}
