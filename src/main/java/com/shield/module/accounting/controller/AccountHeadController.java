package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.accounting.dto.AccountHeadCreateRequest;
import com.shield.module.accounting.dto.AccountHeadResponse;
import com.shield.module.accounting.dto.AccountHeadUpdateRequest;
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
@RequestMapping("/api/v1/account-heads")
@RequiredArgsConstructor
public class AccountHeadController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AccountHeadResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Account heads fetched", accountingTreasuryService.listAccountHeads(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountHeadResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Account head fetched", accountingTreasuryService.getAccountHead(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AccountHeadResponse>> create(@Valid @RequestBody AccountHeadCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Account head created", accountingTreasuryService.createAccountHead(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AccountHeadResponse>> update(@PathVariable UUID id, @Valid @RequestBody AccountHeadUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Account head updated", accountingTreasuryService.updateAccountHead(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        accountingTreasuryService.deleteAccountHead(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Account head deleted", null));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<AccountHeadResponse>>> byType(@PathVariable String type, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Account heads by type fetched", accountingTreasuryService.listAccountHeadsByType(type, pageable)));
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<List<AccountHeadResponse>>> hierarchy() {
        return ResponseEntity.ok(ApiResponse.ok("Account head hierarchy fetched", accountingTreasuryService.listAccountHeadHierarchy()));
    }
}
