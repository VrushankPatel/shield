package com.shield.module.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.marketplace.dto.MarketplaceInquiryCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceInquiryResponse;
import com.shield.module.marketplace.dto.MarketplaceListingCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceListingResponse;
import com.shield.module.marketplace.entity.MarketplaceInquiryEntity;
import com.shield.module.marketplace.entity.MarketplaceListingEntity;
import com.shield.module.marketplace.repository.MarketplaceCategoryRepository;
import com.shield.module.marketplace.repository.MarketplaceInquiryRepository;
import com.shield.module.marketplace.repository.MarketplaceListingRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private MarketplaceCategoryRepository marketplaceCategoryRepository;

    @Mock
    private MarketplaceListingRepository marketplaceListingRepository;

    @Mock
    private MarketplaceInquiryRepository marketplaceInquiryRepository;

    @Mock
    private AuditLogService auditLogService;

    private MarketplaceService marketplaceService;

    @BeforeEach
    void setUp() {
        marketplaceService = new MarketplaceService(
                marketplaceCategoryRepository,
                marketplaceListingRepository,
                marketplaceInquiryRepository,
                auditLogService);
    }

    @Test
    void createListingShouldSetPostedByFromPrincipal() {
        when(marketplaceListingRepository.save(any(MarketplaceListingEntity.class))).thenAnswer(invocation -> {
            MarketplaceListingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        MarketplaceListingCreateRequest request = new MarketplaceListingCreateRequest(
                null,
                "SELL",
                "Cycle for sale",
                "Well maintained mountain bike",
                null,
                true,
                null,
                null,
                null);

        MarketplaceListingResponse response = marketplaceService.createListing(request, principal);

        assertEquals(principal.userId(), response.postedBy());
        assertEquals("ACTIVE", response.status().name());
    }

    @Test
    void createInquiryShouldSetInquiredByFromPrincipal() {
        UUID listingId = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));

        when(marketplaceInquiryRepository.save(any(MarketplaceInquiryEntity.class))).thenAnswer(invocation -> {
            MarketplaceInquiryEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "buyer@shield.dev", "TENANT");
        MarketplaceInquiryCreateRequest request = new MarketplaceInquiryCreateRequest("Is this item still available?");

        MarketplaceInquiryResponse response = marketplaceService.createInquiry(listingId, request, principal);

        assertEquals(principal.userId(), response.inquiredBy());
        assertEquals(listingId, response.listingId());
    }
}
