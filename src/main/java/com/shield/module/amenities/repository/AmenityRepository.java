package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<AmenityEntity, UUID> {

    Optional<AmenityEntity> findByIdAndDeletedFalse(UUID id);

    Page<AmenityEntity> findAllByDeletedFalse(Pageable pageable);
}
