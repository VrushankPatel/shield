package com.shield.module.asset.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset_category")
public class AssetCategoryEntity extends TenantAwareEntity {

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(length = 1000)
    private String description;
}
