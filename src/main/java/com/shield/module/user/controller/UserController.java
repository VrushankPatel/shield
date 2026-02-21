package com.shield.module.user.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.role.dto.UserPermissionsResponse;
import com.shield.module.role.dto.UserRoleAssignRequest;
import com.shield.module.role.service.RoleService;
import com.shield.module.user.dto.UserBulkImportRequest;
import com.shield.module.user.dto.UserBulkImportResponse;
import com.shield.module.user.dto.UserCreateRequest;
import com.shield.module.user.dto.UserResponse;
import com.shield.module.user.dto.UserStatusUpdateRequest;
import com.shield.module.user.dto.UserUpdateRequest;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.service.UserService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

    private final UserService userService;
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.list(pageable)));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listByUnit(
            @PathVariable UUID unitId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users by unit fetched", userService.listByUnit(unitId, pageable)));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> listByRole(
            @PathVariable UserRole role,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users by role fetched", userService.listByRole(role, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated", userService.updateStatus(id, request)));
    }

    @PostMapping("/bulk-import")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<UserBulkImportResponse>> bulkImport(@Valid @RequestBody UserBulkImportRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Users bulk import processed", userService.bulkImport(request)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<String> export(@RequestParam(required = false, defaultValue = "users.csv") String filename) {
        String safeFilename = filename.isBlank() ? "users.csv" : filename;
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + safeFilename)
                .body(userService.exportCsv());
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody UserRoleAssignRequest request,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        roleService.assignRoleToUser(id, request.roleId(), principal == null ? null : principal.userId());
        return ResponseEntity.ok(ApiResponse.ok("User role assigned", null));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal ShieldPrincipal principal) {
        roleService.removeRoleFromUser(id, roleId, principal == null ? null : principal.userId());
        return ResponseEntity.ok(ApiResponse.ok("User role removed", null));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getPermissions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User permissions fetched", roleService.getUserPermissions(id)));
    }
}
