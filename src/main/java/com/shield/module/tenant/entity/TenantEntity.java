package com.shield.module.tenant.entity;

import com.shield.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenant")
public class TenantEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;
}
