package com.shield.module.config.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.config.dto.TenantConfigBulkUpdateRequest;
import com.shield.module.config.dto.TenantConfigResponse;
import com.shield.module.config.dto.TenantConfigUpsertRequest;
import com.shield.module.config.service.TenantConfigService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/config")
@PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
@RequiredArgsConstructor
public class TenantConfigController {

    private final TenantConfigService tenantConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TenantConfigResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant config fetched", tenantConfigService.list(pageable)));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PagedResponse<TenantConfigResponse>>> listByCategory(
            @PathVariable @Size(max = 50) String category,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant config by category fetched", tenantConfigService.listByCategory(category, pageable)));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<TenantConfigResponse>> getByKey(@PathVariable @Size(max = 100) String key) {
        return ResponseEntity.ok(ApiResponse.ok("Tenant config fetched", tenantConfigService.getByKey(key)));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<TenantConfigResponse>> upsert(
            @PathVariable @Size(max = 100) String key,
            @Valid @RequestBody TenantConfigUpsertRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Tenant config upserted", tenantConfigService.upsert(key, request, principal)));
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<ApiResponse<List<TenantConfigResponse>>> bulkUpdate(
            @Valid @RequestBody TenantConfigBulkUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Tenant config bulk updated", tenantConfigService.bulkUpdate(request, principal)));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteByKey(@PathVariable @Size(max = 100) String key) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        tenantConfigService.deleteByKey(key, principal);
        return ResponseEntity.ok(ApiResponse.ok("Tenant config deleted", null));
    }
}
