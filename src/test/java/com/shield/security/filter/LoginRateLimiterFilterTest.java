package com.shield.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimiterFilterTest {

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private ObjectProvider<LoginRateLimiterStore> provider(LoginRateLimiterStore store) {
        @SuppressWarnings("unchecked")
        ObjectProvider<LoginRateLimiterStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(store);
        return provider;
    }

    @Test
    void shouldApplyRateLimitToOtpSendEndpoint() throws Exception {
        LoginRateLimiterStore store = mock(LoginRateLimiterStore.class);
        when(store.incrementAndGet(anyString(), any(Instant.class))).thenReturn(1, 2);
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), provider(store), 1, 60);

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("POST", "/api/v1/auth/login/otp/send");
        firstRequest.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockFilterChain firstChain = new MockFilterChain();

        filter.doFilter(firstRequest, firstResponse, firstChain);
        assertEquals(200, firstResponse.getStatus());
        assertNotNull(firstChain.getRequest());

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("POST", "/api/v1/auth/login/otp/send");
        secondRequest.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        MockFilterChain secondChain = new MockFilterChain();

        filter.doFilter(secondRequest, secondResponse, secondChain);
        assertEquals(429, secondResponse.getStatus());
        assertTrue(secondResponse.getContentAsString().contains("Too many login attempts"));
        assertNull(secondChain.getRequest());
    }

    @Test
    void shouldSkipRateLimitForOtherEndpoints() throws Exception {
        LoginRateLimiterStore store = mock(LoginRateLimiterStore.class);
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), provider(store), 1, 60);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tenants");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void shouldApplyRateLimitToRootLoginEndpoint() throws Exception {
        LoginRateLimiterStore store = mock(LoginRateLimiterStore.class);
        when(store.incrementAndGet(anyString(), any(Instant.class))).thenReturn(1, 2);
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), provider(store), 1, 60);

        MockHttpServletRequest firstRequest = new MockHttpServletRequest("POST", "/api/v1/platform/root/login");
        firstRequest.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockFilterChain firstChain = new MockFilterChain();

        filter.doFilter(firstRequest, firstResponse, firstChain);
        assertEquals(200, firstResponse.getStatus());
        assertNotNull(firstChain.getRequest());

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("POST", "/api/v1/platform/root/login");
        secondRequest.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        MockFilterChain secondChain = new MockFilterChain();

        filter.doFilter(secondRequest, secondResponse, secondChain);
        assertEquals(429, secondResponse.getStatus());
        assertTrue(secondResponse.getContentAsString().contains("Too many login attempts"));
        assertNull(secondChain.getRequest());
    }
}
