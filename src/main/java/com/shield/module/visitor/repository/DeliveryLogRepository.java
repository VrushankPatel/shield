package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.DeliveryLogEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLogEntity, UUID> {

    Optional<DeliveryLogEntity> findByIdAndDeletedFalse(UUID id);

    Page<DeliveryLogEntity> findAllByDeletedFalse(Pageable pageable);

    Page<DeliveryLogEntity> findAllByUnitIdAndDeletedFalse(UUID unitId, Pageable pageable);

    Page<DeliveryLogEntity> findAllByDeliveryPartnerIgnoreCaseAndDeletedFalse(String deliveryPartner, Pageable pageable);

    Page<DeliveryLogEntity> findAllByDeliveryTimeBetweenAndDeletedFalse(Instant from, Instant to, Pageable pageable);
}
