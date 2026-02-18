package com.shield.module.marketplace.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.marketplace.dto.MarketplaceInquiryCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceInquiryResponse;
import com.shield.module.marketplace.dto.MarketplaceListingCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceListingResponse;
import com.shield.module.marketplace.dto.MarketplaceListingUpdateRequest;
import com.shield.module.marketplace.entity.MarketplaceListingStatus;
import com.shield.module.marketplace.service.MarketplaceService;
import com.shield.security.model.ShieldPrincipal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marketplace-listings")
@RequiredArgsConstructor
public class MarketplaceListingController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listings fetched", marketplaceService.listListings(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing fetched", marketplaceService.getListing(id)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> listByCategory(
            @PathVariable UUID categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Category marketplace listings fetched",
                marketplaceService.listListingsByCategory(categoryId, pageable)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> listByStatus(
            @PathVariable MarketplaceListingStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Status marketplace listings fetched",
                marketplaceService.listListingsByStatus(status, pageable)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> listByType(
            @PathVariable String type,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Type marketplace listings fetched",
                marketplaceService.listListingsByType(type, pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> search(
            @RequestParam("q") String query,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listings search fetched",
                marketplaceService.searchListings(query, pageable)));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceListingResponse>>> listMyListings(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("My marketplace listings fetched",
                marketplaceService.listMyListings(principal.userId(), pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> create(
            @Valid @RequestBody MarketplaceListingCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing created", marketplaceService.createListing(request, principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MarketplaceListingUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing updated", marketplaceService.updateListing(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        marketplaceService.deleteListing(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing deleted", null));
    }

    @PostMapping("/{id}/mark-sold")
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> markSold(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing marked sold", marketplaceService.markSold(id, principal)));
    }

    @PostMapping("/{id}/mark-inactive")
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> markInactive(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing marked inactive", marketplaceService.markInactive(id, principal)));
    }

    @PostMapping("/{id}/increment-views")
    public ResponseEntity<ApiResponse<MarketplaceListingResponse>> incrementViews(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing view incremented", marketplaceService.incrementViews(id, principal)));
    }

    @GetMapping("/{id}/inquiries")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceInquiryResponse>>> listInquiriesByListing(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Marketplace listing inquiries fetched",
                marketplaceService.listInquiriesByListing(id, pageable)));
    }

    @PostMapping("/{id}/inquiries")
    public ResponseEntity<ApiResponse<MarketplaceInquiryResponse>> createInquiry(
            @PathVariable UUID id,
            @Valid @RequestBody MarketplaceInquiryCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Marketplace inquiry created",
                marketplaceService.createInquiry(id, request, principal)));
    }
}
