package com.shield.module.visitor.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "domestic_help_registry")
public class DomesticHelpEntity extends TenantAwareEntity {

    @Column(name = "help_name", nullable = false, length = 255)
    private String helpName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "help_type", length = 50)
    private String helpType;

    @Column(name = "permanent_pass", nullable = false)
    private boolean permanentPass;

    @Column(name = "police_verification_done", nullable = false)
    private boolean policeVerificationDone;

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @Column(name = "registered_by", columnDefinition = "uuid")
    private UUID registeredBy;
}
