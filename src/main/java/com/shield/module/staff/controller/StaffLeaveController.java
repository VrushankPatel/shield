package com.shield.module.staff.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.staff.dto.StaffLeaveBalanceResponse;
import com.shield.module.staff.dto.StaffLeaveCreateRequest;
import com.shield.module.staff.dto.StaffLeaveResponse;
import com.shield.module.staff.dto.StaffLeaveUpdateRequest;
import com.shield.module.staff.service.StaffLeaveService;
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
@RequestMapping("/api/v1/staff-leaves")
@RequiredArgsConstructor
public class StaffLeaveController {

    private final StaffLeaveService staffLeaveService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffLeaveResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Staff leaves fetched", staffLeaveService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffLeaveResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Staff leave fetched", staffLeaveService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffLeaveResponse>> create(@Valid @RequestBody StaffLeaveCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff leave created", staffLeaveService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffLeaveResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StaffLeaveUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff leave updated", staffLeaveService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        staffLeaveService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Staff leave deleted", null));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffLeaveResponse>> approve(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff leave approved", staffLeaveService.approve(id, principal)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<StaffLeaveResponse>> reject(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Staff leave rejected", staffLeaveService.reject(id, principal)));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<PagedResponse<StaffLeaveResponse>>> listByStaff(
            @PathVariable UUID staffId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Staff leaves by staff fetched", staffLeaveService.listByStaff(staffId, pageable)));
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponse<PagedResponse<StaffLeaveResponse>>> pendingApprovals(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending staff leaves fetched", staffLeaveService.listPending(pageable)));
    }

    @GetMapping("/balance/{staffId}")
    public ResponseEntity<ApiResponse<StaffLeaveBalanceResponse>> balance(@PathVariable UUID staffId) {
        return ResponseEntity.ok(ApiResponse.ok("Staff leave balance fetched", staffLeaveService.getBalance(staffId)));
    }
}
