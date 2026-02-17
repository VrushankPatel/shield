package com.shield.module.asset.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset")
public class AssetEntity extends TenantAwareEntity {

    @Column(name = "asset_code", nullable = false, length = 80)
    private String assetCode;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
}
