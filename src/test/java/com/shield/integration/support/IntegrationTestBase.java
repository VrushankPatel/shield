package com.shield.integration.support;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("shield_test")
            .withUsername("shield")
            .withPassword("shield");

    static {
        POSTGRES.start();
    }

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("shield.security.jwt.secret", () -> "integration-test-secret-key-minimum-32-bytes");
        registry.add("shield.auth.login-rate-limit.requests", () -> 1000);
        registry.add("shield.auth.login-rate-limit.window-seconds", () -> 1);
    }

    @BeforeEach
    void setupBase() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        truncateAll();
    }

    private void truncateAll() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    payment,
                    maintenance_bill,
                    ledger_entry,
                    visitor_pass,
                    complaint,
                    amenity_booking,
                    amenity,
                    meeting,
                    asset,
                    users,
                    unit,
                    audit_log,
                    tenant
                RESTART IDENTITY CASCADE
                """);
    }
}
