package com.shield.module.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.marketplace.dto.MarketplaceCategoryCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceCategoryResponse;
import com.shield.module.marketplace.dto.MarketplaceCategoryUpdateRequest;
import com.shield.module.marketplace.dto.MarketplaceInquiryCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceInquiryResponse;
import com.shield.module.marketplace.dto.MarketplaceListingCreateRequest;
import com.shield.module.marketplace.dto.MarketplaceListingResponse;
import com.shield.module.marketplace.dto.MarketplaceListingUpdateRequest;
import com.shield.module.marketplace.entity.MarketplaceCategoryEntity;
import com.shield.module.marketplace.entity.MarketplaceInquiryEntity;
import com.shield.module.marketplace.entity.MarketplaceListingEntity;
import com.shield.module.marketplace.entity.MarketplaceListingStatus;
import com.shield.module.marketplace.repository.MarketplaceCategoryRepository;
import com.shield.module.marketplace.repository.MarketplaceInquiryRepository;
import com.shield.module.marketplace.repository.MarketplaceListingRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    void createListingShouldThrowWhenCategoryMissing() {
        UUID categoryId = UUID.randomUUID();
        when(marketplaceCategoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(Optional.empty());

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        MarketplaceListingCreateRequest request = new MarketplaceListingCreateRequest(
                categoryId,
                "SELL",
                "Microwave",
                "Unused",
                BigDecimal.valueOf(2000),
                false,
                null,
                null,
                null);

        assertThrows(ResourceNotFoundException.class, () -> marketplaceService.createListing(request, principal));
    }

    @Test
    void updateListingShouldRejectNonOwnerWithoutPrivilegedRole() {
        UUID listingId = UUID.randomUUID();
        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setPostedBy(UUID.randomUUID());
        listing.setStatus(MarketplaceListingStatus.ACTIVE);
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        MarketplaceListingUpdateRequest request = new MarketplaceListingUpdateRequest(
                null,
                "SELL",
                "Updated title",
                "Updated",
                BigDecimal.valueOf(1500),
                true,
                null,
                null,
                Instant.parse("2026-12-31T00:00:00Z"));

        assertThrows(UnauthorizedException.class, () -> marketplaceService.updateListing(listingId, request, principal));
    }

    @Test
    void updateListingShouldAllowAdminAndPersistChanges() {
        UUID listingId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setPostedBy(UUID.randomUUID());
        listing.setStatus(MarketplaceListingStatus.ACTIVE);
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));

        MarketplaceCategoryEntity category = new MarketplaceCategoryEntity();
        category.setId(categoryId);
        when(marketplaceCategoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(Optional.of(category));
        when(marketplaceListingRepository.save(any(MarketplaceListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        MarketplaceListingResponse response = marketplaceService.updateListing(
                listingId,
                new MarketplaceListingUpdateRequest(
                        categoryId,
                        "RENT",
                        "Banquet utensils",
                        "Full set",
                        BigDecimal.valueOf(500),
                        false,
                        "img1.jpg",
                        UUID.randomUUID(),
                        Instant.parse("2026-12-31T00:00:00Z")),
                principal);

        assertEquals("RENT", response.listingType());
        assertEquals("Banquet utensils", response.title());
        assertEquals(categoryId, response.categoryId());
    }

    @Test
    void markSoldShouldUpdateStatusForOwner() {
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setPostedBy(ownerId);
        listing.setStatus(MarketplaceListingStatus.ACTIVE);
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));
        when(marketplaceListingRepository.save(any(MarketplaceListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(ownerId, UUID.randomUUID(), "owner@shield.dev", "TENANT");
        MarketplaceListingResponse response = marketplaceService.markSold(listingId, principal);

        assertEquals(MarketplaceListingStatus.SOLD, response.status());
    }

    @Test
    void incrementViewsShouldIncreaseCount() {
        UUID listingId = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setViewsCount(2);
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));
        when(marketplaceListingRepository.save(any(MarketplaceListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "viewer@shield.dev", "TENANT");
        MarketplaceListingResponse response = marketplaceService.incrementViews(listingId, principal);

        assertEquals(3, response.viewsCount());
    }

    @Test
    void deleteListingShouldSoftDeleteWhenOwner() {
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setPostedBy(ownerId);

        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.of(listing));
        when(marketplaceListingRepository.save(any(MarketplaceListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(ownerId, UUID.randomUUID(), "owner@shield.dev", "TENANT");
        marketplaceService.deleteListing(listingId, principal);

        assertTrue(listing.isDeleted());
        verify(marketplaceListingRepository).save(listing);
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

    @Test
    void createInquiryShouldThrowWhenListingMissing() {
        UUID listingId = UUID.randomUUID();
        when(marketplaceListingRepository.findByIdAndDeletedFalse(listingId)).thenReturn(Optional.empty());
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "buyer@shield.dev", "TENANT");
        MarketplaceInquiryCreateRequest request = new MarketplaceInquiryCreateRequest("Interested");

        assertThrows(ResourceNotFoundException.class, () -> marketplaceService.createInquiry(listingId, request, principal));
    }

    @Test
    void categoryCrudShouldMapEntities() {
        UUID categoryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");

        when(marketplaceCategoryRepository.save(any(MarketplaceCategoryEntity.class))).thenAnswer(invocation -> {
            MarketplaceCategoryEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(categoryId);
            }
            return entity;
        });

        MarketplaceCategoryResponse created = marketplaceService.createCategory(
                new MarketplaceCategoryCreateRequest("Furniture", "Chairs and tables"),
                principal);
        assertEquals("Furniture", created.categoryName());
        assertEquals(tenantId, created.tenantId());

        MarketplaceCategoryEntity existing = new MarketplaceCategoryEntity();
        existing.setId(categoryId);
        existing.setTenantId(tenantId);
        existing.setCategoryName("Furniture");
        existing.setDescription("Chairs and tables");
        when(marketplaceCategoryRepository.findByIdAndDeletedFalse(categoryId)).thenReturn(Optional.of(existing));

        MarketplaceCategoryResponse updated = marketplaceService.updateCategory(
                categoryId,
                new MarketplaceCategoryUpdateRequest("Electronics", "Appliances"),
                principal);
        assertEquals("Electronics", updated.categoryName());

        MarketplaceCategoryResponse fetched = marketplaceService.getCategory(categoryId);
        assertEquals(categoryId, fetched.id());

        marketplaceService.deleteCategory(categoryId, principal);
        assertTrue(existing.isDeleted());
    }

    @Test
    void listEndpointsShouldReturnMappedPages() {
        UUID listingId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID postedBy = UUID.randomUUID();

        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(listingId);
        listing.setCategoryId(categoryId);
        listing.setPostedBy(postedBy);
        listing.setListingType("SELL");
        listing.setTitle("Dining Table");
        listing.setStatus(MarketplaceListingStatus.ACTIVE);

        MarketplaceInquiryEntity inquiry = new MarketplaceInquiryEntity();
        inquiry.setId(UUID.randomUUID());
        inquiry.setListingId(listingId);
        inquiry.setInquiredBy(UUID.randomUUID());
        inquiry.setMessage("Available?");

        when(marketplaceListingRepository.findAllByCategoryIdAndDeletedFalse(categoryId, Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(listing)));
        when(marketplaceListingRepository.findAllByPostedByAndDeletedFalse(postedBy, Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(listing)));
        when(marketplaceListingRepository.findAllByStatusAndDeletedFalse(MarketplaceListingStatus.ACTIVE, Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(listing)));
        when(marketplaceListingRepository.findAllByListingTypeIgnoreCaseAndDeletedFalse("SELL", Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(listing)));
        when(marketplaceInquiryRepository.findAllByListingIdAndDeletedFalse(listingId, Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(inquiry)));
        when(marketplaceInquiryRepository.findAllByInquiredByAndDeletedFalse(inquiry.getInquiredBy(), Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(List.of(inquiry)));

        assertEquals(1, marketplaceService.listListingsByCategory(categoryId, Pageable.ofSize(10)).content().size());
        assertEquals(1, marketplaceService.listMyListings(postedBy, Pageable.ofSize(10)).content().size());
        assertEquals(1, marketplaceService.listListingsByStatus(MarketplaceListingStatus.ACTIVE, Pageable.ofSize(10)).content().size());
        assertEquals(1, marketplaceService.listListingsByType("SELL", Pageable.ofSize(10)).content().size());
        assertEquals(1, marketplaceService.listInquiriesByListing(listingId, Pageable.ofSize(10)).content().size());
        assertEquals(1, marketplaceService.listMyInquiries(inquiry.getInquiredBy(), Pageable.ofSize(10)).content().size());
    }

    @Test
    void searchListingsShouldReturnMatchingResults() {
        MarketplaceListingEntity listing = new MarketplaceListingEntity();
        listing.setId(UUID.randomUUID());
        listing.setListingType("SELL");
        listing.setTitle("Dining Table");

        when(marketplaceListingRepository.searchByText("table", Pageable.ofSize(10)))
                .thenReturn(new PageImpl<>(java.util.List.of(listing)));

        PagedResponse<MarketplaceListingResponse> page = marketplaceService.searchListings("table", Pageable.ofSize(10));

        assertEquals(1, page.content().size());
        assertEquals("Dining Table", page.content().get(0).title());
    }
}
