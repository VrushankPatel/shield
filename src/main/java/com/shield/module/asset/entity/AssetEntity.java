package com.shield.module.asset.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset")
public class AssetEntity extends TenantAwareEntity {

    @Column(name = "asset_code", nullable = false, length = 80)
    private String assetCode;

    @Column(name = "asset_name", length = 255)
    private String assetName;

    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 255)
    private String location;

    @Column(name = "block_name", length = 50)
    private String blockName;

    @Column(name = "floor_label", length = 50)
    private String floorLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "amc_applicable", nullable = false)
    private boolean amcApplicable;

    @Column(name = "amc_vendor_id", columnDefinition = "uuid")
    private UUID amcVendorId;

    @Column(name = "amc_start_date")
    private LocalDate amcStartDate;

    @Column(name = "amc_end_date")
    private LocalDate amcEndDate;

    @Column(name = "purchase_cost", precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Column(name = "current_value", precision = 12, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "qr_code_data", length = 500)
    private String qrCodeData;
}
