package com.shield.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shield.common.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimiterFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int maxRequests;
    private final int windowSeconds;

    public LoginRateLimiterFilter(
            ObjectMapper objectMapper,
            @Value("${shield.auth.login-rate-limit.requests}") int maxRequests,
            @Value("${shield.auth.login-rate-limit.window-seconds}") int windowSeconds) {
        this.objectMapper = objectMapper;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long threshold = now - (windowSeconds * 1000L);

        Deque<Long> window = requestWindows.computeIfAbsent(ip, key -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && window.peekFirst() < threshold) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                ErrorResponse error = new ErrorResponse(
                        Instant.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                        "Too many login attempts. Try again later.",
                        request.getRequestURI());

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }
            window.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/v1/auth/login".equals(request.getRequestURI());
    }
}
