package com.shield.module.staff.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.staff.dto.StaffCreateRequest;
import com.shield.module.staff.dto.StaffResponse;
import com.shield.module.staff.dto.StaffStatusUpdateRequest;
import com.shield.module.staff.dto.StaffUpdateRequest;
import com.shield.module.staff.service.StaffService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Staff list fetched", staffService.list(pageable)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<StaffResponse>>> listActive(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active staff list fetched", staffService.listActive(pageable)));
    }

    @GetMapping("/designation/{designation}")
    public ResponseEntity<ApiResponse<PagedResponse<StaffResponse>>> listByDesignation(
            @PathVariable String designation,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Designation staff list fetched",
                staffService.listByDesignation(designation, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Staff fetched", staffService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffResponse>> create(@Valid @RequestBody StaffCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff created", staffService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StaffUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff updated", staffService.update(id, request, principal)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StaffStatusUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff status updated", staffService.updateStatus(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        staffService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Staff deleted", null));
    }
}
