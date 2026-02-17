package com.shield.module.marketplace.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "marketplace_inquiry")
public class MarketplaceInquiryEntity extends TenantAwareEntity {

    @Column(name = "listing_id", nullable = false, columnDefinition = "uuid")
    private UUID listingId;

    @Column(name = "inquired_by", columnDefinition = "uuid")
    private UUID inquiredBy;

    @Column(length = 2000)
    private String message;
}
