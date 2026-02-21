package com.shield.module.marketplace.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.marketplace.dto.CarpoolListingCreateRequest;
import com.shield.module.marketplace.dto.CarpoolListingResponse;
import com.shield.module.marketplace.dto.CarpoolListingUpdateRequest;
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
@RequestMapping("/api/v1/carpool-listings")
@RequiredArgsConstructor
public class CarpoolListingController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CarpoolListingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Carpool listings fetched", marketplaceService.listCarpoolListings(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarpoolListingResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Carpool listing fetched", marketplaceService.getCarpoolListing(id)));
    }

    @GetMapping("/route")
    public ResponseEntity<ApiResponse<PagedResponse<CarpoolListingResponse>>> listByRoute(
            @RequestParam String from,
            @RequestParam String to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Carpool listings by route fetched",
                marketplaceService.listCarpoolListingsByRoute(from, to, pageable)));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<PagedResponse<CarpoolListingResponse>>> listMyListings(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("My carpool listings fetched",
                marketplaceService.listMyCarpoolListings(principal.userId(), pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CarpoolListingResponse>> create(@Valid @RequestBody CarpoolListingCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Carpool listing created", marketplaceService.createCarpoolListing(request, principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CarpoolListingResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CarpoolListingUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Carpool listing updated", marketplaceService.updateCarpoolListing(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        marketplaceService.deleteCarpoolListing(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Carpool listing deleted", null));
    }
}
