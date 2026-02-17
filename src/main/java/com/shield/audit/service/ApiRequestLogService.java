package com.shield.audit.service;

import com.shield.audit.entity.ApiRequestLogEntity;
import com.shield.audit.repository.ApiRequestLogRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiRequestLogService {

    private final ApiRequestLogRepository apiRequestLogRepository;

    public ApiRequestLogService(ApiRequestLogRepository apiRequestLogRepository) {
        this.apiRequestLogRepository = apiRequestLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String requestId,
            UUID tenantId,
            UUID userId,
            String endpoint,
            String httpMethod,
            String requestBody,
            Integer responseStatus,
            Long responseTimeMs,
            String ipAddress,
            String userAgent) {

        ApiRequestLogEntity entity = new ApiRequestLogEntity();
        entity.setRequestId(requestId);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setEndpoint(endpoint);
        entity.setHttpMethod(httpMethod);
        entity.setRequestBody(requestBody);
        entity.setResponseStatus(responseStatus);
        entity.setResponseTimeMs(responseTimeMs);
        entity.setIpAddress(ipAddress);
        entity.setUserAgent(userAgent);
        apiRequestLogRepository.save(entity);
    }
}
