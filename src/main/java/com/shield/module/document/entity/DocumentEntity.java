package com.shield.module.document.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "document")
public class DocumentEntity extends TenantAwareEntity {

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "file_url", nullable = false, length = 2000)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 1000)
    private String description;

    @Column(name = "version_label", length = 50)
    private String versionLabel;

    @Column(name = "public_access", nullable = false)
    private boolean publicAccess;

    @Column(name = "uploaded_by", columnDefinition = "uuid")
    private UUID uploadedBy;

    @Column(name = "upload_date", nullable = false)
    private Instant uploadDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(length = 500)
    private String tags;
}
