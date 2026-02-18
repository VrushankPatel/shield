package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.BlacklistEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<BlacklistEntity, UUID> {

    Optional<BlacklistEntity> findByIdAndDeletedFalse(UUID id);

    Page<BlacklistEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<BlacklistEntity> findByPhoneAndActiveTrueAndDeletedFalse(String phone);
}
