package com.shield.audit.filter;

import com.shield.audit.service.ApiRequestLogService;
import com.shield.common.logging.CorrelationIdFilter;
import com.shield.security.model.ShieldPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_USER_AGENT = 1000;
    private static final int MAX_ENDPOINT = 255;

    private final ApiRequestLogService apiRequestLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/api/v1")) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        Exception downstreamException = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            downstreamException = ex;
            throw ex;
        } finally {
            long duration = Math.max(System.currentTimeMillis() - start, 0L);
            int status = response.getStatus();
            if (downstreamException != null && status < 400) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            try {
                UUID tenantId = null;
                UUID userId = null;
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof ShieldPrincipal principal) {
                    tenantId = principal.tenantId();
                    userId = principal.userId();
                }

                String requestId = response.getHeader(CorrelationIdFilter.HEADER);
                if (requestId == null || requestId.isBlank()) {
                    requestId = request.getHeader(CorrelationIdFilter.HEADER);
                }

                apiRequestLogService.record(
                        requestId,
                        tenantId,
                        userId,
                        truncate(request.getRequestURI(), MAX_ENDPOINT),
                        request.getMethod(),
                        null,
                        status,
                        duration,
                        resolveClientIp(request),
                        truncate(request.getHeader("User-Agent"), MAX_USER_AGENT));
            } catch (Exception ignored) {
                // Logging failures must never break API flow.
            }
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isBlank()) {
                return first;
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String truncate(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }
}
