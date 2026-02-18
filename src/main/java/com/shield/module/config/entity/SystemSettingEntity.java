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
@Table(name = "system_setting")
public class SystemSettingEntity extends TenantAwareEntity {

    @Column(name = "setting_key", nullable = false, length = 120)
    private String settingKey;

    @Column(name = "setting_value")
    private String settingValue;

    @Column(name = "setting_group", length = 80)
    private String settingGroup;
}
