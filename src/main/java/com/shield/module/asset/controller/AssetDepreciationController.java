package com.shield.module.asset.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.module.asset.dto.AssetDepreciationCalculateRequest;
import com.shield.module.asset.dto.AssetDepreciationReportRow;
import com.shield.module.asset.dto.AssetDepreciationResponse;
import com.shield.module.asset.service.AssetDepreciationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset-depreciation")
@RequiredArgsConstructor
public class AssetDepreciationController {

    private final AssetDepreciationService assetDepreciationService;

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResponse<List<AssetDepreciationResponse>>> byAsset(@PathVariable UUID assetId) {
        return ResponseEntity.ok(ApiResponse.ok("Asset depreciation entries fetched", assetDepreciationService.listByAsset(assetId)));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<AssetDepreciationResponse>> calculate(
            @Valid @RequestBody AssetDepreciationCalculateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Asset depreciation calculated", assetDepreciationService.calculate(request)));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<AssetDepreciationResponse>>> byYear(@PathVariable Integer year) {
        return ResponseEntity.ok(ApiResponse.ok("Asset depreciation entries by year fetched", assetDepreciationService.listByYear(year)));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<AssetDepreciationReportRow>>> report() {
        return ResponseEntity.ok(ApiResponse.ok("Asset depreciation report fetched", assetDepreciationService.report()));
    }
}
