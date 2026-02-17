package com.shield.module.tenant.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.tenant.dto.TenantCreateRequest;
import com.shield.module.tenant.dto.TenantResponse;
import com.shield.module.tenant.dto.TenantUpdateRequest;
import com.shield.module.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> create(@Valid @RequestBody TenantCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant created", tenantService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant fetched", tenantService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TenantUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant updated", tenantService.update(id, request)));
    }
}
