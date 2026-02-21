package com.shield.audit.service;

import com.shield.audit.entity.ApiRequestLogEntity;
import com.shield.audit.repository.ApiRequestLogRepository;
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
    public void logRequest(ApiRequestLogCommand command) {
        ApiRequestLogEntity entity = new ApiRequestLogEntity();
        entity.setRequestId(command.requestId());
        entity.setTenantId(command.tenantId());
        entity.setUserId(command.userId());
        entity.setEndpoint(command.endpoint());
        entity.setHttpMethod(command.httpMethod());
        entity.setRequestBody(command.requestBody());
        entity.setResponseStatus(command.responseStatus());
        entity.setResponseTimeMs(command.responseTimeMs());
        entity.setIpAddress(command.ipAddress());
        entity.setUserAgent(command.userAgent());
        apiRequestLogRepository.save(entity);
    }
}
