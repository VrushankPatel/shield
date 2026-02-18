package com.shield.module.parking.repository;

import com.shield.module.parking.entity.ParkingSlotEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlotEntity, UUID> {

    Optional<ParkingSlotEntity> findByIdAndDeletedFalse(UUID id);

    Page<ParkingSlotEntity> findAllByDeletedFalse(Pageable pageable);

    Page<ParkingSlotEntity> findAllByAllocatedFalseAndDeletedFalse(Pageable pageable);

    Page<ParkingSlotEntity> findAllByUnitIdAndDeletedFalse(UUID unitId, Pageable pageable);

    boolean existsBySlotNumberIgnoreCaseAndDeletedFalse(String slotNumber);
}
