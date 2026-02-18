package com.shield.module.accounting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vendor")
public class VendorEntity extends TenantAwareEntity {

    @Column(name = "vendor_name", nullable = false, length = 255)
    private String vendorName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 1000)
    private String address;

    @Column(length = 50)
    private String gstin;

    @Column(length = 50)
    private String pan;

    @Column(name = "vendor_type", length = 100)
    private String vendorType;

    @Column(nullable = false)
    private boolean active;
}
