package com.shield.module.role.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.role.dto.PermissionResponse;
import com.shield.module.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
public class PermissionController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PermissionResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Permissions fetched", roleService.listPermissions(pageable)));
    }
}
