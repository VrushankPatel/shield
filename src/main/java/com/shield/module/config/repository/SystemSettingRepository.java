package com.shield.module.config.repository;

import com.shield.module.config.entity.SystemSettingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSettingEntity, UUID> {

    Optional<SystemSettingEntity> findFirstBySettingKeyAndDeletedFalseOrderByCreatedAtDesc(String settingKey);

    List<SystemSettingEntity> findAllBySettingGroupAndDeletedFalse(String settingGroup);
}
