package com.shield.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.common.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimiterFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final LoginRateLimiterStore rateLimiterStore;
    private final int maxRequests;
    private final int windowSeconds;

    public LoginRateLimiterFilter(
            ObjectMapper objectMapper,
            LoginRateLimiterStore rateLimiterStore,
            @Value("${shield.auth.login-rate-limit.requests}") int maxRequests,
            @Value("${shield.auth.login-rate-limit.window-seconds}") int windowSeconds) {
        this.objectMapper = objectMapper;
        this.rateLimiterStore = rateLimiterStore;
        this.maxRequests = maxRequests;
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Instant now = Instant.now();
        long windowBucket = now.getEpochSecond() / windowSeconds;
        Instant windowStart = Instant.ofEpochSecond(windowBucket * windowSeconds);
        String bucketKey = request.getRequestURI() + "|" + ip + "|" + windowStart.getEpochSecond();
        int count = rateLimiterStore.incrementAndGet(bucketKey, windowStart);

        if (count > maxRequests) {
            ErrorResponse error = new ErrorResponse(
                    now,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Too many login attempts. Try again later.",
                    request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return "/api/v1/auth/login".equals(uri)
                || "/api/v1/auth/login/otp/send".equals(uri)
                || "/api/v1/platform/root/login".equals(uri);
    }
}
