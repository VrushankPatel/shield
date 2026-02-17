package com.shield.module.marketplace.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.marketplace.dto.MarketplaceCategoryCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceCategoryResponse;
import com.shield.module.marketplace.dto.MarketplaceCategoryUpdateRequest;
import com.shield.module.marketplace.service.MarketplaceService;
import com.shield.security.model.ShieldPrincipal;
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
@RequestMapping("/api/v1/marketplace-categories")
@RequiredArgsConstructor
public class MarketplaceCategoryController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceCategoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace categories fetched", marketplaceService.listCategories(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarketplaceCategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace category fetched", marketplaceService.getCategory(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MarketplaceCategoryResponse>> create(
            @Valid @RequestBody MarketplaceCategoryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace category created", marketplaceService.createCategory(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<MarketplaceCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MarketplaceCategoryUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace category updated", marketplaceService.updateCategory(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        marketplaceService.deleteCategory(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Marketplace category deleted", null));
    }
}
