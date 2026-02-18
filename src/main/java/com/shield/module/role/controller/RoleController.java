package com.shield.module.role.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.role.dto.RoleCreateRequest;
import com.shield.module.role.dto.RolePermissionAssignRequest;
import com.shield.module.role.dto.RoleResponse;
import com.shield.module.role.dto.RoleUpdateRequest;
import com.shield.module.role.service.RoleService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RoleResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Roles fetched", roleService.listRoles(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Role fetched", roleService.getRole(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody RoleCreateRequest request,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Role created",
                roleService.createRole(request, principal == null ? null : principal.userId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Role updated",
                roleService.updateRole(id, request, principal == null ? null : principal.userId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        roleService.deleteRole(id, principal == null ? null : principal.userId());
        return ResponseEntity.ok(ApiResponse.ok("Role deleted", null));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody RolePermissionAssignRequest request,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        roleService.assignPermissions(id, request, principal == null ? null : principal.userId());
        return ResponseEntity.ok(ApiResponse.ok("Role permissions assigned", null));
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable UUID id,
            @PathVariable UUID permissionId,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        roleService.removePermission(id, permissionId, principal == null ? null : principal.userId());
        return ResponseEntity.ok(ApiResponse.ok("Role permission removed", null));
    }
}
