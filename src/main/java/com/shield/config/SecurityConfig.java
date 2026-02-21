package com.shield.config;

import com.shield.audit.filter.ApiRequestLoggingFilter;
import com.shield.common.logging.CorrelationIdFilter;
import com.shield.security.filter.JwtAuthenticationFilter;
import com.shield.security.filter.LoginRateLimiterFilter;
import com.shield.security.filter.RestAccessDeniedHandler;
import com.shield.security.filter.RestAuthenticationEntryPoint;
import com.shield.tenant.filter.TenantContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final LoginRateLimiterFilter loginRateLimiterFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantContextFilter tenantContextFilter;
    private final ApiRequestLoggingFilter apiRequestLoggingFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${shield.security.cors.allowed-origins:http://localhost:3000,http://localhost:19006}")
    private String corsAllowedOrigins;

    @Value("${shield.security.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String corsAllowedMethods;

    @Value("${shield.security.cors.allowed-headers:Authorization,Content-Type,X-Correlation-Id}")
    private String corsAllowedHeaders;

    @Value("${shield.security.cors.exposed-headers:X-Correlation-Id}")
    private String corsExposedHeaders;

    @Value("${shield.security.cors.allow-credentials:true}")
    private boolean corsAllowCredentials;

    @Value("${shield.security.headers.content-security-policy:default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'; form-action 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'}")
    private String contentSecurityPolicy;

    @Value("${shield.security.headers.hsts-enabled:false}")
    private boolean hstsEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> {
                    headers.frameOptions(frameOptions -> frameOptions.deny());
                    headers.contentTypeOptions(contentTypeOptions -> {
                    });
                    headers.referrerPolicy(referrerPolicy -> referrerPolicy
                            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
                    headers.permissionsPolicy(policy -> policy
                            .policy("geolocation=(), microphone=(), camera=(), payment=()"));
                    headers.contentSecurityPolicy(csp -> csp.policyDirectives(contentSecurityPolicy));
                    if (hstsEnabled) {
                        headers.httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000));
                    }
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/prometheus")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/login/otp/send",
                                "/api/v1/auth/login/otp/verify",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/logout",
                                "/api/v1/platform/root/login",
                                "/api/v1/platform/root/refresh",
                                "/api/v1/payments/webhook/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/verify-email/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tenants").hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(correlationIdFilter, LogoutFilter.class)
                .addFilterBefore(loginRateLimiterFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantContextFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(apiRequestLoggingFilter, TenantContextFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseCsv(corsAllowedOrigins));
        configuration.setAllowedMethods(parseCsv(corsAllowedMethods));
        configuration.setAllowedHeaders(parseCsv(corsAllowedHeaders));
        configuration.setExposedHeaders(parseCsv(corsExposedHeaders));
        configuration.setAllowCredentials(corsAllowCredentials);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
