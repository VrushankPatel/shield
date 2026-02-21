package com.shield.audit.service;

import com.shield.audit.entity.AuditLogEntity;
import com.shield.audit.repository.AuditLogRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logEvent(UUID tenantId, UUID userId, String action, String entityType, UUID entityId, String payload) {
        AuditLogEntity log = new AuditLogEntity();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPayload(payload);
        auditLogRepository.save(log);
    }
}
