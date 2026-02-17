package com.shield.module.marketplace.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.marketplace.dto.MarketplaceInquiryResponse;
import com.shield.module.marketplace.service.MarketplaceService;
import com.shield.security.model.ShieldPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marketplace-inquiries")
@RequiredArgsConstructor
public class MarketplaceInquiryController {

    private final MarketplaceService marketplaceService;

    @GetMapping("/my-inquiries")
    public ResponseEntity<ApiResponse<PagedResponse<MarketplaceInquiryResponse>>> listMyInquiries(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("My marketplace inquiries fetched",
                marketplaceService.listMyInquiries(principal.userId(), pageable)));
    }
}
