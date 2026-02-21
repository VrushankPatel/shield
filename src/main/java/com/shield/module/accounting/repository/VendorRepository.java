package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.VendorEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<VendorEntity, UUID> {

    Optional<VendorEntity> findByIdAndDeletedFalse(UUID id);

    Page<VendorEntity> findAllByDeletedFalse(Pageable pageable);

    Page<VendorEntity> findAllByVendorTypeIgnoreCaseAndDeletedFalse(String vendorType, Pageable pageable);

    Page<VendorEntity> findAllByActiveAndDeletedFalse(boolean active, Pageable pageable);
}
