package com.shield.module.accounting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account_head")
public class AccountHeadEntity extends TenantAwareEntity {

    @Column(name = "head_name", nullable = false, length = 100)
    private String headName;

    @Enumerated(EnumType.STRING)
    @Column(name = "head_type", nullable = false, length = 50)
    private AccountHeadType headType;

    @Column(name = "parent_head_id", columnDefinition = "uuid")
    private UUID parentHeadId;
}
