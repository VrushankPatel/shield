package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.VisitorPassEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorPassRepository extends JpaRepository<VisitorPassEntity, UUID> {

    Optional<VisitorPassEntity> findByIdAndDeletedFalse(UUID id);
}
