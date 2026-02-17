package com.shield.module.marketplace.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "marketplace_listing")
public class MarketplaceListingEntity extends TenantAwareEntity {

    @Column(name = "listing_number", nullable = false, length = 100)
    private String listingNumber;

    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "listing_type", nullable = false, length = 50)
    private String listingType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean negotiable;

    @Column(length = 2000)
    private String images;

    @Column(name = "posted_by", columnDefinition = "uuid")
    private UUID postedBy;

    @Column(name = "unit_id", columnDefinition = "uuid")
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MarketplaceListingStatus status;

    @Column(name = "views_count", nullable = false)
    private int viewsCount;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
