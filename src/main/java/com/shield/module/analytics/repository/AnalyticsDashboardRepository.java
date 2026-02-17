package com.shield.module.analytics.repository;

import com.shield.module.analytics.entity.AnalyticsDashboardEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsDashboardRepository extends JpaRepository<AnalyticsDashboardEntity, UUID> {

    Page<AnalyticsDashboardEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AnalyticsDashboardEntity> findByIdAndDeletedFalse(UUID id);

    Page<AnalyticsDashboardEntity> findAllByDashboardTypeAndDeletedFalse(String dashboardType, Pageable pageable);

    List<AnalyticsDashboardEntity> findAllByDefaultDashboardTrueAndDeletedFalse();
}
