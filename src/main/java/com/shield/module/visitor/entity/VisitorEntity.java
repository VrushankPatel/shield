package com.shield.module.visitor.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visitor")
public class VisitorEntity extends TenantAwareEntity {

    @Column(name = "visitor_name", nullable = false, length = 255)
    private String visitorName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "visitor_type", length = 50)
    private String visitorType;

    @Column(name = "id_proof_type", length = 50)
    private String idProofType;

    @Column(name = "id_proof_number", length = 100)
    private String idProofNumber;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;
}
