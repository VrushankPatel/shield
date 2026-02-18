package com.shield.module.accounting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.accounting.dto.FinancialReportResponse;
import com.shield.module.accounting.service.AccountingTreasuryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class FinancialReportController {

    private final AccountingTreasuryService accountingTreasuryService;

    @GetMapping("/income-statement")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> incomeStatement(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("Income statement fetched", accountingTreasuryService.incomeStatement(financialYear)));
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> balanceSheet(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("Balance sheet fetched", accountingTreasuryService.balanceSheet(financialYear)));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> cashFlow(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("Cash flow fetched", accountingTreasuryService.cashFlow(financialYear)));
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> trialBalance(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("Trial balance fetched", accountingTreasuryService.trialBalance(financialYear)));
    }

    @GetMapping("/fund-summary")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> fundSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Fund summary fetched", accountingTreasuryService.fundSummary()));
    }

    @GetMapping("/export/ca-format")
    public ResponseEntity<ApiResponse<String>> exportCaFormat(@RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(ApiResponse.ok("CA format export generated", accountingTreasuryService.exportCaFormat(financialYear)));
    }
}
