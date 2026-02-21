package com.shield.module.payroll.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.payroll.dto.SalaryStructureCreateRequest;
import com.shield.module.payroll.dto.SalaryStructureResponse;
import com.shield.module.payroll.dto.SalaryStructureUpdateRequest;
import com.shield.module.payroll.service.SalaryStructureService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @GetMapping("/staff/{staffId}/salary-structure")
    public ResponseEntity<ApiResponse<PagedResponse<SalaryStructureResponse>>> listByStaff(
            @PathVariable UUID staffId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Salary structures fetched", salaryStructureService.listByStaff(staffId, pageable)));
    }

    @PostMapping("/staff/{staffId}/salary-structure")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> create(
            @PathVariable UUID staffId,
            @Valid @RequestBody SalaryStructureCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Salary structure created", salaryStructureService.create(staffId, request, principal)));
    }

    @PutMapping("/salary-structure/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SalaryStructureUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Salary structure updated", salaryStructureService.update(id, request, principal)));
    }

    @DeleteMapping("/salary-structure/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        salaryStructureService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Salary structure deleted", null));
    }
}
