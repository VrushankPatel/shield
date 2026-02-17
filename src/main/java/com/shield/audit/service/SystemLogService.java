package com.shield.audit.service;

import com.shield.audit.entity.SystemLogEntity;
import com.shield.audit.repository.SystemLogRepository;
import com.shield.security.model.ShieldPrincipal;
import com.shield.tenant.context.TenantContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemLogService {

    private static final String CORRELATION_MDC_KEY = "correlationId";

    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWarn(String loggerName, String message, Throwable throwable, String endpoint) {
        record("WARN", loggerName, message, throwable, endpoint);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordError(String loggerName, String message, Throwable throwable, String endpoint) {
        record("ERROR", loggerName, message, throwable, endpoint);
    }

    private void record(String level, String loggerName, String message, Throwable throwable, String endpoint) {
        SystemLogEntity entity = new SystemLogEntity();
        entity.setTenantId(TenantContext.getTenantId().orElse(null));
        entity.setUserId(resolveCurrentUserId());
        entity.setLogLevel(level);
        entity.setLoggerName(nonBlankOrDefault(loggerName, "unknown"));
        entity.setMessage(nonBlankOrDefault(message, "n/a"));
        entity.setExceptionTrace(stackTraceOf(throwable));
        entity.setEndpoint(endpoint);
        entity.setCorrelationId(MDC.get(CORRELATION_MDC_KEY));
        systemLogRepository.save(entity);
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ShieldPrincipal principal)) {
            return null;
        }
        return principal.userId();
    }

    private String stackTraceOf(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private String nonBlankOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
