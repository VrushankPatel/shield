package com.shield.module.auth.repository;

import com.shield.module.auth.entity.AuthTokenEntity;
import com.shield.module.auth.entity.AuthTokenType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthTokenEntity, UUID> {

    Optional<AuthTokenEntity> findByTokenValueAndDeletedFalse(String tokenValue);

    List<AuthTokenEntity> findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
            UUID tenantId,
            UUID userId,
            AuthTokenType tokenType);
}
