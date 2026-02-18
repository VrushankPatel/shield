package com.shield.module.role.repository;

import com.shield.module.role.entity.UserAdditionalRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAdditionalRoleRepository extends JpaRepository<UserAdditionalRoleEntity, UUID> {

    List<UserAdditionalRoleEntity> findAllByUserIdAndDeletedFalse(UUID userId);

    Optional<UserAdditionalRoleEntity> findByUserIdAndRoleIdAndDeletedFalse(UUID userId, UUID roleId);
}
