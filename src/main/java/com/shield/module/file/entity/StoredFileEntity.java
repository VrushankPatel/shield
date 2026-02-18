package com.shield.module.file.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stored_file")
public class StoredFileEntity extends TenantAwareEntity {

    @Column(name = "file_id", nullable = false, unique = true, length = 120)
    private String fileId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_path", nullable = false, length = 2000)
    private String storagePath;

    @Column(name = "uploaded_by", columnDefinition = "uuid")
    private UUID uploadedBy;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StoredFileStatus status = StoredFileStatus.ACTIVE;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
