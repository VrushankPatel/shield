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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

    private final AssetService assetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AssetResponse>> create(@Valid @RequestBody AssetCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset created", assetService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Assets fetched", assetService.list(pageable)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> listByCategory(
            @PathVariable UUID categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Assets by category fetched", assetService.listByCategory(categoryId, pageable)));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> listByLocation(
            @PathVariable String location,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Assets by location fetched", assetService.listByLocation(location, pageable)));
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<ApiResponse<AssetResponse>> getByTag(@PathVariable String tag) {
        return ResponseEntity.ok(ApiResponse.ok("Asset fetched", assetService.getByTag(tag)));
    }

    @GetMapping("/verify-qr/{qrCode}")
    public ResponseEntity<ApiResponse<AssetResponse>> verifyQr(@PathVariable String qrCode) {
        return ResponseEntity.ok(ApiResponse.ok("Asset verified", assetService.verifyQr(qrCode)));
    }

    @GetMapping("/amc-expiring")
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> amcExpiring(
            @RequestParam(name = "days", defaultValue = "30") int days,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("AMC expiring assets fetched", assetService.listAmcExpiring(days, pageable)));
    }

    @GetMapping("/warranty-expiring")
    public ResponseEntity<ApiResponse<PagedResponse<AssetResponse>>> warrantyExpiring(
            @RequestParam(name = "days", defaultValue = "30") int days,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Warranty expiring assets fetched", assetService.listWarrantyExpiring(days, pageable)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<String> export(@RequestParam(required = false, defaultValue = "assets.csv") String filename) {
        String safeFilename = filename.isBlank() ? "assets.csv" : filename;
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + safeFilename)
                .body(assetService.exportCsv());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Asset fetched", assetService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssetUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset updated", assetService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        assetService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Asset deleted", null));
    }
}
