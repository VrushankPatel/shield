package com.shield.module.config.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenant_config")
public class TenantConfigEntity extends TenantAwareEntity {

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "category", length = 50)
    private String category;
}
