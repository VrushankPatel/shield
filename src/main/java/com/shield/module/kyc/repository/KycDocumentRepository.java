package com.shield.module.kyc.repository;

import com.shield.module.kyc.entity.KycDocumentEntity;
import com.shield.module.kyc.entity.KycVerificationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycDocumentRepository extends JpaRepository<KycDocumentEntity, UUID> {

    Optional<KycDocumentEntity> findByIdAndDeletedFalse(UUID id);

    Page<KycDocumentEntity> findAllByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Page<KycDocumentEntity> findAllByVerificationStatusAndDeletedFalse(KycVerificationStatus status, Pageable pageable);
}
