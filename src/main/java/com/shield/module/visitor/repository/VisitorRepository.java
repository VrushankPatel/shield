package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.VisitorEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitorRepository extends JpaRepository<VisitorEntity, UUID> {

    Optional<VisitorEntity> findByIdAndDeletedFalse(UUID id);

    Page<VisitorEntity> findAllByDeletedFalse(Pageable pageable);

    Page<VisitorEntity> findAllByPhoneAndDeletedFalse(String phone, Pageable pageable);

    @Query("""
            select v from VisitorEntity v
            where v.deleted = false
              and (
                    lower(v.visitorName) like lower(concat('%', :query, '%'))
                    or lower(v.phone) like lower(concat('%', :query, '%'))
                  )
            """)
    Page<VisitorEntity> search(@Param("query") String query, Pageable pageable);
}
