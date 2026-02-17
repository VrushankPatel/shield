package com.shield.module.document.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "document_category")
public class DocumentCategoryEntity extends TenantAwareEntity {

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(length = 500)
    private String description;

    @Column(name = "parent_category_id", columnDefinition = "uuid")
    private UUID parentCategoryId;
}
