package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityCancellationPolicyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityCancellationPolicyRepository extends JpaRepository<AmenityCancellationPolicyEntity, UUID> {

    Optional<AmenityCancellationPolicyEntity> findByIdAndDeletedFalse(UUID id);

    Optional<AmenityCancellationPolicyEntity> findFirstByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(UUID amenityId);
}
