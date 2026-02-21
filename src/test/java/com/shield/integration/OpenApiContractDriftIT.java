package com.shield.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.shield.integration.support.IntegrationTestBase;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.yaml.snakeyaml.Yaml;

class OpenApiContractDriftIT extends IntegrationTestBase {

    private static final String API_BASE_PATH = "/api/v1";
    private static final Pattern PATH_VARIABLE_WITH_REGEX = Pattern.compile("\\{([^}:]+):[^}]+}");
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    OpenApiContractDriftIT(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Test
    void openApiContractShouldMatchRuntimeMappings() {
        Set<String> contractEndpoints = loadContractEndpoints();
        Set<String> runtimeEndpoints = loadRuntimeEndpoints();

        List<String> missingInRuntime = contractEndpoints.stream()
                .filter(endpoint -> !runtimeEndpoints.contains(endpoint))
                .toList();
        List<String> undocumentedRuntimeEndpoints = runtimeEndpoints.stream()
                .filter(endpoint -> !contractEndpoints.contains(endpoint))
                .toList();

        assertThat(missingInRuntime)
                .as("Contract paths not implemented in runtime mappings")
                .isEmpty();
        assertThat(undocumentedRuntimeEndpoints)
                .as("Runtime paths missing from openapi.yml")
                .isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Set<String> loadContractEndpoints() {
        ClassPathResource resource = new ClassPathResource("openapi.yml");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> document = new Yaml().load(inputStream);
            Map<String, Object> paths = (Map<String, Object>) document.getOrDefault("paths", Map.of());
            Set<String> endpoints = new TreeSet<>();

            for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
                String normalizedPath = normalizePath(API_BASE_PATH + pathEntry.getKey());
                if (!(pathEntry.getValue() instanceof Map<?, ?> operations)) {
                    continue;
                }

                for (Object operationKey : operations.keySet()) {
                    String httpMethod = String.valueOf(operationKey).toUpperCase(Locale.ROOT);
                    if (HTTP_METHODS.contains(httpMethod)) {
                        endpoints.add(httpMethod + " " + normalizedPath);
                    }
                }
            }
            return endpoints;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load openapi.yml for contract drift test", ex);
        }
    }

    private Set<String> loadRuntimeEndpoints() {
        Set<String> endpoints = new TreeSet<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        for (RequestMappingInfo mappingInfo : handlerMethods.keySet()) {
            Set<String> paths = resolvePaths(mappingInfo);
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

            if (methods.isEmpty()) {
                continue;
            }

            for (RequestMethod requestMethod : methods) {
                for (String path : paths) {
                    String normalizedPath = normalizePath(path);
                    if (normalizedPath.startsWith(API_BASE_PATH + "/")) {
                        endpoints.add(requestMethod.name() + " " + normalizedPath);
                    }
                }
            }
        }

        return endpoints;
    }

    @SuppressWarnings("deprecation")
    private Set<String> resolvePaths(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            return new HashSet<>(mappingInfo.getPathPatternsCondition().getPatternValues());
        }
        if (mappingInfo.getPatternsCondition() != null) {
            return new HashSet<>(mappingInfo.getPatternsCondition().getPatterns());
        }
        return Set.of();
    }

    private String normalizePath(String rawPath) {
        String normalized = PATH_VARIABLE_WITH_REGEX.matcher(rawPath).replaceAll("{$1}");
        normalized = normalized.replaceAll("//+", "/");
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
