package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import com.shield.integration.support.IntegrationTestBase;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.entity.UnitStatus;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class ObservabilityLogsIT extends IntegrationTestBase {

    private static final String PASSWORD = "password123";

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void observabilityFlowShouldExposeAuditSystemAndApiRequestLogsWithTenantIsolation() {
        TenantEntity tenantOne = createTenant("Logs Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "A-110");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.logs.one@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("Logs Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "B-220");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.logs.two@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String staffId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "employeeId", "OBS-STF-01",
                        "firstName", "Nisha",
                        "lastName", "Guard",
                        "phone", "9999999911",
                        "email", "nisha.guard@shield.dev",
                        "designation", "SECURITY_GUARD",
                        "dateOfJoining", LocalDate.of(2026, 2, 1).toString(),
                        "employmentType", "FULL_TIME",
                        "basicSalary", 22000,
                        "active", true))
                .when()
                .post("/staff")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        String expectedMissingStaffId = UUID.randomUUID().toString();
        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/staff/{id}", expectedMissingStaffId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        String auditLogId = given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/audit-logs/action/{action}", "STAFF_CREATED")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1))
                .body("data.content[0].action", equalTo("STAFF_CREATED"))
                .extract()
                .path("data.content[0].id");

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/audit-logs/entity/{entityType}/{entityId}", "staff", staffId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        String from = Instant.now().minusSeconds(3600).toString();
        String to = Instant.now().plusSeconds(3600).toString();

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/audit-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/audit-logs/export")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/audit-logs/{id}", auditLogId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        String systemLogId = given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/system-logs/level/{level}", "WARN")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1))
                .extract()
                .path("data.content[0].id");

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/system-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/system-logs/export")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/system-logs/{id}", systemLogId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        String apiRequestLogId = given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/api-request-logs/failed-requests")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1))
                .extract()
                .path("data.content[0].id");

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/api-request-logs/endpoint/{endpoint}", "staff")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/api-request-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("thresholdMs", 1)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/api-request-logs/slow-requests")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/api-request-logs/export")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/api-request-logs/{id}", apiRequestLogId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private TenantEntity createTenant(String name) {
        TenantEntity entity = new TenantEntity();
        entity.setName(name);
        entity.setAddress("Integration Address");
        return tenantRepository.save(entity);
    }

    private UnitEntity createUnit(UUID tenantId, String unitNumber) {
        UnitEntity unit = new UnitEntity();
        unit.setTenantId(tenantId);
        unit.setUnitNumber(unitNumber);
        unit.setBlock("A");
        unit.setType("FLAT");
        unit.setSquareFeet(BigDecimal.valueOf(1000));
        unit.setStatus(UnitStatus.ACTIVE);
        return unitRepository.save(unit);
    }

    private UserEntity createUser(UUID tenantId, UUID unitId, String name, String email, UserRole role) {
        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setUnitId(unitId);
        user.setName(name);
        user.setEmail(email);
        user.setPhone("9999999999");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.accessToken");
    }
}
