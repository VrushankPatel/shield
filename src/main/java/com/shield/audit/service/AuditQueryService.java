package com.shield.audit.service;

import com.shield.audit.dto.ApiRequestLogResponse;
import com.shield.audit.dto.AuditLogResponse;
import com.shield.audit.dto.SystemLogResponse;
import com.shield.audit.entity.ApiRequestLogEntity;
import com.shield.audit.entity.AuditLogEntity;
import com.shield.audit.entity.SystemLogEntity;
import com.shield.audit.repository.ApiRequestLogRepository;
import com.shield.audit.repository.AuditLogRepository;
import com.shield.audit.repository.SystemLogRepository;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.tenant.context.TenantContext;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;
    private final SystemLogRepository systemLogRepository;
    private final ApiRequestLogRepository apiRequestLogRepository;

    public AuditQueryService(
            AuditLogRepository auditLogRepository,
            SystemLogRepository systemLogRepository,
            ApiRequestLogRepository apiRequestLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.systemLogRepository = systemLogRepository;
        this.apiRequestLogRepository = apiRequestLogRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> listAuditLogs(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(auditLogRepository.findAllByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::toAuditLogResponse));
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLog(UUID id) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        AuditLogEntity entity = auditLogRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id));
        return toAuditLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> listAuditLogsByUser(UUID userId, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(auditLogRepository.findAllByTenantIdAndUserIdAndDeletedFalse(tenantId, userId, pageable)
                .map(this::toAuditLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> listAuditLogsByEntity(String entityType, UUID entityId, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(auditLogRepository.findAllByTenantIdAndEntityTypeIgnoreCaseAndEntityIdAndDeletedFalse(
                        tenantId,
                        entityType,
                        entityId,
                        pageable)
                .map(this::toAuditLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> listAuditLogsByAction(String action, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(auditLogRepository.findAllByTenantIdAndActionAndDeletedFalse(tenantId, normalize(action), pageable)
                .map(this::toAuditLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> listAuditLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        validateRange(from, to);
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(auditLogRepository.findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(tenantId, from, to, pageable)
                .map(this::toAuditLogResponse));
    }

    @Transactional(readOnly = true)
    public String exportAuditLogsCsv() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        List<AuditLogEntity> logs = auditLogRepository.findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);

        StringBuilder csv = new StringBuilder();
        csv.append("id,tenantId,userId,action,entityType,entityId,payload,createdAt\n");
        for (AuditLogEntity log : logs) {
            csv.append(csv(log.getId())).append(',')
                    .append(csv(log.getTenantId())).append(',')
                    .append(csv(log.getUserId())).append(',')
                    .append(csv(log.getAction())).append(',')
                    .append(csv(log.getEntityType())).append(',')
                    .append(csv(log.getEntityId())).append(',')
                    .append(csv(log.getPayload())).append(',')
                    .append(csv(log.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SystemLogResponse> listSystemLogs(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(systemLogRepository.findAllByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::toSystemLogResponse));
    }

    @Transactional(readOnly = true)
    public SystemLogResponse getSystemLog(UUID id) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        SystemLogEntity entity = systemLogRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("System log not found: " + id));
        return toSystemLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SystemLogResponse> listSystemLogsByLevel(String level, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(systemLogRepository.findAllByTenantIdAndLogLevelAndDeletedFalse(
                        tenantId,
                        normalize(level),
                        pageable)
                .map(this::toSystemLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SystemLogResponse> listSystemLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        validateRange(from, to);
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(systemLogRepository.findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(tenantId, from, to, pageable)
                .map(this::toSystemLogResponse));
    }

    @Transactional(readOnly = true)
    public String exportSystemLogsCsv() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        List<SystemLogEntity> logs = systemLogRepository.findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);

        StringBuilder csv = new StringBuilder();
        csv.append("id,tenantId,userId,logLevel,loggerName,message,endpoint,correlationId,createdAt\n");
        for (SystemLogEntity log : logs) {
            csv.append(csv(log.getId())).append(',')
                    .append(csv(log.getTenantId())).append(',')
                    .append(csv(log.getUserId())).append(',')
                    .append(csv(log.getLogLevel())).append(',')
                    .append(csv(log.getLoggerName())).append(',')
                    .append(csv(log.getMessage())).append(',')
                    .append(csv(log.getEndpoint())).append(',')
                    .append(csv(log.getCorrelationId())).append(',')
                    .append(csv(log.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listApiRequestLogs(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public ApiRequestLogResponse getApiRequestLog(UUID id) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        ApiRequestLogEntity entity = apiRequestLogRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("API request log not found: " + id));
        return toApiRequestLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listApiRequestLogsByUser(UUID userId, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndUserIdAndDeletedFalse(tenantId, userId, pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listApiRequestLogsByEndpoint(String endpoint, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        String decodedEndpoint = URLDecoder.decode(endpoint, StandardCharsets.UTF_8);
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndEndpointContainingIgnoreCaseAndDeletedFalse(
                        tenantId,
                        decodedEndpoint,
                        pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listApiRequestLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        validateRange(from, to);
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(tenantId, from, to, pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listSlowApiRequests(long thresholdMs, Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndResponseTimeMsGreaterThanEqualAndDeletedFalse(
                        tenantId,
                        thresholdMs,
                        pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApiRequestLogResponse> listFailedApiRequests(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        return PagedResponse.from(apiRequestLogRepository.findAllByTenantIdAndResponseStatusGreaterThanEqualAndDeletedFalse(
                        tenantId,
                        400,
                        pageable)
                .map(this::toApiRequestLogResponse));
    }

    @Transactional(readOnly = true)
    public String exportApiRequestLogsCsv() {
        UUID tenantId = TenantContext.getRequiredTenantId();
        List<ApiRequestLogEntity> logs = apiRequestLogRepository.findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);

        StringBuilder csv = new StringBuilder();
        csv.append("id,requestId,tenantId,userId,endpoint,httpMethod,responseStatus,responseTimeMs,ipAddress,createdAt\n");
        for (ApiRequestLogEntity log : logs) {
            csv.append(csv(log.getId())).append(',')
                    .append(csv(log.getRequestId())).append(',')
                    .append(csv(log.getTenantId())).append(',')
                    .append(csv(log.getUserId())).append(',')
                    .append(csv(log.getEndpoint())).append(',')
                    .append(csv(log.getHttpMethod())).append(',')
                    .append(csv(log.getResponseStatus())).append(',')
                    .append(csv(log.getResponseTimeMs())).append(',')
                    .append(csv(log.getIpAddress())).append(',')
                    .append(csv(log.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    private AuditLogResponse toAuditLogResponse(AuditLogEntity entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getPayload(),
                entity.getCreatedAt());
    }

    private SystemLogResponse toSystemLogResponse(SystemLogEntity entity) {
        return new SystemLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getLogLevel(),
                entity.getLoggerName(),
                entity.getMessage(),
                entity.getExceptionTrace(),
                entity.getEndpoint(),
                entity.getCorrelationId(),
                entity.getCreatedAt());
    }

    private ApiRequestLogResponse toApiRequestLogResponse(ApiRequestLogEntity entity) {
        return new ApiRequestLogResponse(
                entity.getId(),
                entity.getRequestId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getEndpoint(),
                entity.getHttpMethod(),
                entity.getResponseStatus(),
                entity.getResponseTimeMs(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt());
    }

    private void validateRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("from and to query params are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from must be before or equal to to");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        return '"' + text + '"';
    }
}
