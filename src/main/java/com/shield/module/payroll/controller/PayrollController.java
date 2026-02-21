package com.shield.module.payroll.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.payroll.dto.PayrollBulkProcessRequest;
import com.shield.module.payroll.dto.PayrollGenerateRequest;
import com.shield.module.payroll.dto.PayrollPayslipResponse;
import com.shield.module.payroll.dto.PayrollProcessRequest;
import com.shield.module.payroll.dto.PayrollResponse;
import com.shield.module.payroll.dto.PayrollSummaryResponse;
import com.shield.module.payroll.service.PayrollService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<PayrollResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll list fetched", payrollService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll fetched", payrollService.getById(id)));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> generate(@Valid @RequestBody PayrollGenerateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payroll generated", payrollService.generate(request, principal)));
    }

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> process(@Valid @RequestBody PayrollProcessRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payroll processed", payrollService.process(request, principal)));
    }

    @PostMapping("/bulk-process")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> bulkProcess(@Valid @RequestBody PayrollBulkProcessRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payrolls bulk processed", payrollService.bulkProcess(request, principal)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> approve(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payroll approved", payrollService.approve(id, principal)));
    }

    @GetMapping("/month/{month}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<PayrollResponse>>> listByPeriod(
            @PathVariable int month,
            @PathVariable int year,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll by period fetched", payrollService.listByPeriod(month, year, pageable)));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<PayrollResponse>>> listByStaff(
            @PathVariable UUID staffId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll by staff fetched", payrollService.listByStaff(staffId, pageable)));
    }

    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollPayslipResponse>> payslip(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll payslip fetched", payrollService.getPayslip(id)));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollSummaryResponse>> summary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll summary fetched", payrollService.summarize(month, year)));
    }
}
