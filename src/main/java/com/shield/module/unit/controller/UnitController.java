package com.shield.module.unit.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.unit.dto.UnitCreateRequest;
import com.shield.module.unit.dto.UnitHistoryResponse;
import com.shield.module.unit.dto.UnitOwnershipUpdateRequest;
import com.shield.module.unit.dto.UnitOwnershipUpdateResponse;
import com.shield.module.unit.dto.UnitResponse;
import com.shield.module.unit.dto.UnitUpdateRequest;
import com.shield.module.unit.service.UnitService;
import com.shield.module.user.dto.UserResponse;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<ApiResponse<UnitResponse>> create(@Valid @RequestBody UnitCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Unit created", unitService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UnitResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Units fetched", unitService.list(pageable)));
    }

    @GetMapping("/block/{block}")
    public ResponseEntity<ApiResponse<PagedResponse<UnitResponse>>> listByBlock(
            @PathVariable String block,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Units by block fetched", unitService.listByBlock(block, pageable)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PagedResponse<UnitResponse>>> listAvailable(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Available units fetched", unitService.listAvailable(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Unit fetched", unitService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UnitUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Unit updated", unitService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        unitService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Unit deleted", null));
    }

    @PatchMapping("/{id}/ownership")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<UnitOwnershipUpdateResponse>> updateOwnership(
            @PathVariable UUID id,
            @Valid @RequestBody UnitOwnershipUpdateRequest request,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        UUID changedBy = principal == null ? null : principal.userId();
        return ResponseEntity.ok(ApiResponse.ok(
                "Unit ownership updated",
                unitService.updateOwnership(id, request, changedBy)));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listMembers(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Unit members fetched", unitService.listMembers(id, pageable)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<PagedResponse<UnitHistoryResponse>>> listHistory(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Unit history fetched", unitService.listHistory(id, pageable)));
    }
}
