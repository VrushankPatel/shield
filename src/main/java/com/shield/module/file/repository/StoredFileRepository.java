package com.shield.module.file.repository;

import com.shield.module.file.entity.StoredFileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {

    Optional<StoredFileEntity> findByFileIdAndDeletedFalse(String fileId);
}
