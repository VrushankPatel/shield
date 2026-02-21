package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.DomesticHelpUnitMappingEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomesticHelpUnitMappingRepository extends JpaRepository<DomesticHelpUnitMappingEntity, UUID> {

    Page<DomesticHelpUnitMappingEntity> findAllByUnitIdAndActiveTrueAndDeletedFalse(UUID unitId, Pageable pageable);

    Optional<DomesticHelpUnitMappingEntity> findFirstByDomesticHelpIdAndUnitIdAndActiveTrueAndDeletedFalse(
            UUID domesticHelpId,
            UUID unitId);
}
