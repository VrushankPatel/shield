package com.shield.module.role.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "permission")
public class PermissionEntity extends TenantAwareEntity {

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "module_name", length = 60)
    private String moduleName;

    @Column(length = 500)
    private String description;
}
