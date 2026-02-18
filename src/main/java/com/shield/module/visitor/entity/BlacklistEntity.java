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
@Table(name = "blacklist")
public class BlacklistEntity extends TenantAwareEntity {

    @Column(name = "person_name", length = 255)
    private String personName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "blacklisted_by", columnDefinition = "uuid")
    private UUID blacklistedBy;

    @Column(name = "blacklist_date")
    private LocalDate blacklistDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
