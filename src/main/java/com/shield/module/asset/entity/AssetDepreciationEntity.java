package com.shield.module.asset.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset_depreciation")
public class AssetDepreciationEntity extends TenantAwareEntity {

    @Column(name = "asset_id", nullable = false, columnDefinition = "uuid")
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", nullable = false, length = 40)
    private DepreciationMethod depreciationMethod;

    @Column(name = "depreciation_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal depreciationRate;

    @Column(name = "depreciation_year", nullable = false)
    private Integer depreciationYear;

    @Column(name = "depreciation_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal depreciationAmount;

    @Column(name = "book_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal bookValue;
}
