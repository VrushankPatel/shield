package com.shield.module.marketplace.service;

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
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketplaceService {

    private static final String ENTITY_MARKETPLACE_CATEGORY = "marketplace_category";
    private static final String MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX = "Marketplace category not found: ";
    private static final String ENTITY_MARKETPLACE_LISTING = "marketplace_listing";
    private static final String MARKETPLACE_LISTING_NOT_FOUND_PREFIX = "Marketplace listing not found: ";

    private final MarketplaceCategoryRepository marketplaceCategoryRepository;
    private final MarketplaceListingRepository marketplaceListingRepository;
    private final MarketplaceInquiryRepository marketplaceInquiryRepository;
    private final AuditLogService auditLogService;

    public MarketplaceService(
            MarketplaceCategoryRepository marketplaceCategoryRepository,
            MarketplaceListingRepository marketplaceListingRepository,
            MarketplaceInquiryRepository marketplaceInquiryRepository,
            AuditLogService auditLogService) {
        this.marketplaceCategoryRepository = marketplaceCategoryRepository;
        this.marketplaceListingRepository = marketplaceListingRepository;
        this.marketplaceInquiryRepository = marketplaceInquiryRepository;
        this.auditLogService = auditLogService;
    }

    public MarketplaceCategoryResponse createCategory(MarketplaceCategoryCreateRequest request, ShieldPrincipal principal) {
        MarketplaceCategoryEntity entity = new MarketplaceCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());

        MarketplaceCategoryEntity saved = marketplaceCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_CATEGORY_CREATED", ENTITY_MARKETPLACE_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceCategoryResponse> listCategories(Pageable pageable) {
        return PagedResponse.from(marketplaceCategoryRepository.findAllByDeletedFalse(pageable).map(this::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public MarketplaceCategoryResponse getCategory(UUID id) {
        MarketplaceCategoryEntity entity = marketplaceCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX + id));
        return toCategoryResponse(entity);
    }

    public MarketplaceCategoryResponse updateCategory(UUID id, MarketplaceCategoryUpdateRequest request, ShieldPrincipal principal) {
        MarketplaceCategoryEntity entity = marketplaceCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());

        MarketplaceCategoryEntity saved = marketplaceCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_CATEGORY_UPDATED", ENTITY_MARKETPLACE_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    public void deleteCategory(UUID id, ShieldPrincipal principal) {
        MarketplaceCategoryEntity entity = marketplaceCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        marketplaceCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_CATEGORY_DELETED", ENTITY_MARKETPLACE_CATEGORY, entity.getId(), null);
    }

    public MarketplaceListingResponse createListing(MarketplaceListingCreateRequest request, ShieldPrincipal principal) {
        if (request.categoryId() != null) {
            marketplaceCategoryRepository.findByIdAndDeletedFalse(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX + request.categoryId()));
        }

        MarketplaceListingEntity entity = new MarketplaceListingEntity();
        entity.setTenantId(principal.tenantId());
        entity.setListingNumber("MKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setCategoryId(request.categoryId());
        entity.setListingType(request.listingType());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
        entity.setNegotiable(request.negotiable());
        entity.setImages(request.images());
        entity.setPostedBy(principal.userId());
        entity.setUnitId(request.unitId());
        entity.setStatus(MarketplaceListingStatus.ACTIVE);
        entity.setViewsCount(0);
        entity.setExpiresAt(request.expiresAt());

        MarketplaceListingEntity saved = marketplaceListingRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_LISTING_CREATED", ENTITY_MARKETPLACE_LISTING, saved.getId(), null);
        return toListingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> listListings(Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.findAllByDeletedFalse(pageable).map(this::toListingResponse));
    }

    @Transactional(readOnly = true)
    public MarketplaceListingResponse getListing(UUID id) {
        MarketplaceListingEntity entity = marketplaceListingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + id));
        return toListingResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> listListingsByCategory(UUID categoryId, Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.findAllByCategoryIdAndDeletedFalse(categoryId, pageable)
                .map(this::toListingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> listMyListings(UUID postedBy, Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.findAllByPostedByAndDeletedFalse(postedBy, pageable)
                .map(this::toListingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> listListingsByStatus(MarketplaceListingStatus status, Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.findAllByStatusAndDeletedFalse(status, pageable)
                .map(this::toListingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> listListingsByType(String listingType, Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.findAllByListingTypeIgnoreCaseAndDeletedFalse(listingType, pageable)
                .map(this::toListingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceListingResponse> searchListings(String query, Pageable pageable) {
        return PagedResponse.from(marketplaceListingRepository.searchByText(query, pageable).map(this::toListingResponse));
    }

    public MarketplaceListingResponse updateListing(UUID id, MarketplaceListingUpdateRequest request, ShieldPrincipal principal) {
        MarketplaceListingEntity entity = marketplaceListingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + id));
        assertCanManageListing(principal, entity);

        if (request.categoryId() != null) {
            marketplaceCategoryRepository.findByIdAndDeletedFalse(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_CATEGORY_NOT_FOUND_PREFIX + request.categoryId()));
        }

        entity.setCategoryId(request.categoryId());
        entity.setListingType(request.listingType());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
        entity.setNegotiable(request.negotiable());
        entity.setImages(request.images());
        entity.setUnitId(request.unitId());
        entity.setExpiresAt(request.expiresAt());

        MarketplaceListingEntity saved = marketplaceListingRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_LISTING_UPDATED", ENTITY_MARKETPLACE_LISTING, saved.getId(), null);
        return toListingResponse(saved);
    }

    public void deleteListing(UUID id, ShieldPrincipal principal) {
        MarketplaceListingEntity entity = marketplaceListingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + id));
        assertCanManageListing(principal, entity);

        entity.setDeleted(true);
        marketplaceListingRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_LISTING_DELETED", ENTITY_MARKETPLACE_LISTING, entity.getId(), null);
    }

    public MarketplaceListingResponse markSold(UUID id, ShieldPrincipal principal) {
        return updateStatus(id, MarketplaceListingStatus.SOLD, principal, "MARKETPLACE_LISTING_MARKED_SOLD");
    }

    public MarketplaceListingResponse markInactive(UUID id, ShieldPrincipal principal) {
        return updateStatus(id, MarketplaceListingStatus.INACTIVE, principal, "MARKETPLACE_LISTING_MARKED_INACTIVE");
    }

    public MarketplaceListingResponse incrementViews(UUID id, ShieldPrincipal principal) {
        MarketplaceListingEntity entity = marketplaceListingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + id));
        entity.setViewsCount(entity.getViewsCount() + 1);

        MarketplaceListingEntity saved = marketplaceListingRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_LISTING_VIEW_INCREMENTED", ENTITY_MARKETPLACE_LISTING, saved.getId(), null);
        return toListingResponse(saved);
    }

    public MarketplaceInquiryResponse createInquiry(UUID listingId, MarketplaceInquiryCreateRequest request, ShieldPrincipal principal) {
        marketplaceListingRepository.findByIdAndDeletedFalse(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + listingId));

        MarketplaceInquiryEntity entity = new MarketplaceInquiryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setListingId(listingId);
        entity.setInquiredBy(principal.userId());
        entity.setMessage(request.message());

        MarketplaceInquiryEntity saved = marketplaceInquiryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "MARKETPLACE_INQUIRY_CREATED", "marketplace_inquiry", saved.getId(), null);
        return toInquiryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceInquiryResponse> listInquiriesByListing(UUID listingId, Pageable pageable) {
        return PagedResponse.from(marketplaceInquiryRepository.findAllByListingIdAndDeletedFalse(listingId, pageable)
                .map(this::toInquiryResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MarketplaceInquiryResponse> listMyInquiries(UUID userId, Pageable pageable) {
        return PagedResponse.from(marketplaceInquiryRepository.findAllByInquiredByAndDeletedFalse(userId, pageable)
                .map(this::toInquiryResponse));
    }

    private MarketplaceListingResponse updateStatus(
            UUID id,
            MarketplaceListingStatus status,
            ShieldPrincipal principal,
            String auditAction) {

        MarketplaceListingEntity entity = marketplaceListingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARKETPLACE_LISTING_NOT_FOUND_PREFIX + id));
        assertCanManageListing(principal, entity);

        entity.setStatus(status);
        MarketplaceListingEntity saved = marketplaceListingRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), auditAction, ENTITY_MARKETPLACE_LISTING, saved.getId(), null);
        return toListingResponse(saved);
    }

    private void assertCanManageListing(ShieldPrincipal principal, MarketplaceListingEntity listing) {
        boolean privilegedRole = "ADMIN".equals(principal.role()) || "COMMITTEE".equals(principal.role());
        if (privilegedRole) {
            return;
        }
        if (!principal.userId().equals(listing.getPostedBy())) {
            throw new UnauthorizedException("You are not authorized to manage this listing");
        }
    }

    private MarketplaceCategoryResponse toCategoryResponse(MarketplaceCategoryEntity entity) {
        return new MarketplaceCategoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCategoryName(),
                entity.getDescription());
    }

    private MarketplaceListingResponse toListingResponse(MarketplaceListingEntity entity) {
        return new MarketplaceListingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getListingNumber(),
                entity.getCategoryId(),
                entity.getListingType(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.isNegotiable(),
                entity.getImages(),
                entity.getPostedBy(),
                entity.getUnitId(),
                entity.getStatus(),
                entity.getViewsCount(),
                entity.getExpiresAt());
    }

    private MarketplaceInquiryResponse toInquiryResponse(MarketplaceInquiryEntity entity) {
        return new MarketplaceInquiryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getListingId(),
                entity.getInquiredBy(),
                entity.getMessage(),
                entity.getCreatedAt());
    }
}
