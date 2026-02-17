package com.shield.module.asset.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.asset.dto.AssetCreateRequest;
import com.shield.module.asset.dto.AssetResponse;
import com.shield.module.asset.dto.AssetUpdateRequest;
import com.shield.module.asset.service.AssetService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> create(@Valid @RequestBody AssetCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset created", assetService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Assets fetched", assetService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Asset fetched", assetService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset updated", assetService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        assetService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Asset deleted", null));
    }
}
