package com.shield.module.move.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.move.dto.MoveRecordCreateRequest;
import com.shield.module.move.dto.MoveRecordDecisionRequest;
import com.shield.module.move.dto.MoveRecordResponse;
import com.shield.module.move.service.MoveRecordService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/move-records")
@RequiredArgsConstructor
public class MoveRecordController {

    private final MoveRecordService moveRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<MoveRecordResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Move records fetched", moveRecordService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MoveRecordResponse>> getById(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Move record fetched", moveRecordService.getById(id, principal)));
    }

    @PostMapping("/move-in")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT')")
    public ResponseEntity<ApiResponse<MoveRecordResponse>> moveIn(@Valid @RequestBody MoveRecordCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Move-in request created", moveRecordService.createMoveIn(request, principal)));
    }

    @PostMapping("/move-out")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','OWNER','TENANT')")
    public ResponseEntity<ApiResponse<MoveRecordResponse>> moveOut(@Valid @RequestBody MoveRecordCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Move-out request created", moveRecordService.createMoveOut(request, principal)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MoveRecordResponse>> approve(
            @PathVariable UUID id,
            @Valid @RequestBody MoveRecordDecisionRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Move record approved", moveRecordService.approve(id, request, principal)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MoveRecordResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody MoveRecordDecisionRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Move record rejected", moveRecordService.reject(id, request, principal)));
    }

    @GetMapping("/unit/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<MoveRecordResponse>>> listByUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Move records by unit fetched", moveRecordService.listByUnit(unitId, pageable)));
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PagedResponse<MoveRecordResponse>>> pendingApprovals(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending move records fetched", moveRecordService.listPendingApprovals(pageable)));
    }
}
