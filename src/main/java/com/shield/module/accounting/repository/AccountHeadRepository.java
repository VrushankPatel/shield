package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.AccountHeadEntity;
import com.shield.module.accounting.entity.AccountHeadType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountHeadRepository extends JpaRepository<AccountHeadEntity, UUID> {

    Optional<AccountHeadEntity> findByIdAndDeletedFalse(UUID id);

    Page<AccountHeadEntity> findAllByDeletedFalse(Pageable pageable);

    List<AccountHeadEntity> findAllByDeletedFalseOrderByHeadNameAsc();

    Page<AccountHeadEntity> findAllByHeadTypeAndDeletedFalse(AccountHeadType headType, Pageable pageable);
}
