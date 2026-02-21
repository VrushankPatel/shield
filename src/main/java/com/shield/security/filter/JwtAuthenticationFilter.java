package com.shield.security.filter;

import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import com.shield.module.platform.service.PlatformRootService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";

    private final JwtService jwtService;
    private final PlatformRootService platformRootService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtService.stripBearerPrefix(authHeader);
        try {
            Claims claims = jwtService.parseClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            String principalType = claims.get("principalType", String.class);
            if (!StringUtils.hasText(principalType)) {
                principalType = "USER";
            }

            long tokenVersion = parseTokenVersion(claims.get("tokenVersion"));
            UUID tenantId = null;
            if ("ROOT".equalsIgnoreCase(principalType)) {
                if (!platformRootService.isRootTokenVersionValid(userId, tokenVersion)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                String tenantIdValue = claims.get("tenantId", String.class);
                if (StringUtils.hasText(tenantIdValue)) {
                    tenantId = UUID.fromString(tenantIdValue);
                }
            }

            ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, email, role, principalType, tokenVersion);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/auth/verify-email/")) {
            return true;
        }

        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }

        return "/api/v1/auth/register".equals(path)
                || "/api/v1/auth/login".equals(path)
                || "/api/v1/auth/login/otp/send".equals(path)
                || "/api/v1/auth/login/otp/verify".equals(path)
                || "/api/v1/auth/refresh".equals(path)
                || "/api/v1/auth/refresh-token".equals(path)
                || "/api/v1/auth/forgot-password".equals(path)
                || "/api/v1/auth/reset-password".equals(path)
                || "/api/v1/auth/logout".equals(path)
                || "/api/v1/platform/root/login".equals(path)
                || "/api/v1/platform/root/refresh".equals(path);
    }

    private long parseTokenVersion(Object tokenVersionClaim) {
        if (tokenVersionClaim instanceof Number number) {
            return number.longValue();
        }
        if (tokenVersionClaim instanceof String tokenVersionText && StringUtils.hasText(tokenVersionText)) {
            return Long.parseLong(tokenVersionText);
        }
        return 0L;
    }
}
