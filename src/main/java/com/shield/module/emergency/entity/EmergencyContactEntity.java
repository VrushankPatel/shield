package com.shield.module.emergency.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "emergency_contact")
public class EmergencyContactEntity extends TenantAwareEntity {

    @Column(name = "contact_type", nullable = false, length = 100)
    private String contactType;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(name = "phone_primary", nullable = false, length = 20)
    private String phonePrimary;

    @Column(name = "phone_secondary", length = 20)
    private String phoneSecondary;

    @Column(length = 500)
    private String address;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active;
}
