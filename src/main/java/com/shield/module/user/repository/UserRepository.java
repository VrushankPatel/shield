package com.shield.module.user.repository;

import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCaseAndDeletedFalse(String email);

    Optional<UserEntity> findByIdAndDeletedFalse(UUID id);

    Page<UserEntity> findAllByDeletedFalse(Pageable pageable);

    boolean existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(UUID tenantId, String email);

    List<UserEntity> findAllByTenantIdAndStatusAndDeletedFalse(UUID tenantId, UserStatus status);
}
