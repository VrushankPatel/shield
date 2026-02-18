package com.shield.module.digitalid.repository;

import com.shield.module.digitalid.entity.DigitalIdCardEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalIdCardRepository extends JpaRepository<DigitalIdCardEntity, UUID> {

    Optional<DigitalIdCardEntity> findByIdAndDeletedFalse(UUID id);

    Optional<DigitalIdCardEntity> findByQrCodeDataAndDeletedFalse(String qrCodeData);

    Page<DigitalIdCardEntity> findAllByUserIdAndDeletedFalse(UUID userId, Pageable pageable);
}
