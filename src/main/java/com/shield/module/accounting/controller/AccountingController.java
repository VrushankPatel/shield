package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.accounting.dto.LedgerCreateRequest;
import com.shield.module.accounting.dto.LedgerResponse;
import com.shield.module.accounting.dto.LedgerSummaryResponse;
import com.shield.module.accounting.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    @PostMapping
    public ResponseEntity<ApiResponse<LedgerResponse>> create(@Valid @RequestBody LedgerCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entry created", accountingService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LedgerResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Ledger entries fetched", accountingService.list(pageable)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<LedgerSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.ok("Ledger summary fetched", accountingService.summary()));
    }
}
