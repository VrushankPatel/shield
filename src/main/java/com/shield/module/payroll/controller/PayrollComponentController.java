package com.shield.module.payroll.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.payroll.dto.PayrollComponentCreateRequest;
import com.shield.module.payroll.dto.PayrollComponentResponse;
import com.shield.module.payroll.dto.PayrollComponentUpdateRequest;
import com.shield.module.payroll.service.PayrollComponentService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/payroll-components")
@RequiredArgsConstructor
public class PayrollComponentController {

    private final PayrollComponentService payrollComponentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PayrollComponentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll components fetched", payrollComponentService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollComponentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payroll component fetched", payrollComponentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollComponentResponse>> create(
            @Valid @RequestBody PayrollComponentCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payroll component created", payrollComponentService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PayrollComponentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PayrollComponentUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Payroll component updated", payrollComponentService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        payrollComponentService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Payroll component deleted", null));
    }
}
