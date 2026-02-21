package com.shield.module.asset.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.asset.dto.AssetCategoryCreateRequest;
import com.shield.module.asset.dto.AssetCategoryResponse;
import com.shield.module.asset.dto.AssetCategoryUpdateRequest;
import com.shield.module.asset.service.AssetCategoryService;
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
@RequestMapping("/api/v1/asset-categories")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AssetCategoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Asset categories fetched", assetCategoryService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetCategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Asset category fetched", assetCategoryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AssetCategoryResponse>> create(@Valid @RequestBody AssetCategoryCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset category created", assetCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AssetCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetCategoryUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset category updated", assetCategoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        assetCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Asset category deleted", null));
    }
}
