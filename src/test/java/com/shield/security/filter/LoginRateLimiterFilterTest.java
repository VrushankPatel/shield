package com.shield.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimiterFilterTest {

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldApplyRateLimitToOtpSendEndpoint() throws Exception {
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), 1, 60);

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
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), 1, 60);

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
        LoginRateLimiterFilter filter = new LoginRateLimiterFilter(objectMapper(), 1, 60);

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
