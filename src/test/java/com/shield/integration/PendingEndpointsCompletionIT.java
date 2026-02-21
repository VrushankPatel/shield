package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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

class PendingEndpointsCompletionIT extends IntegrationTestBase {

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
    void missingEndpointsShouldWorkEndToEnd() {
        TenantEntity tenant = createTenant("Pending Endpoint Society");
        UnitEntity unit = createUnit(tenant.getId(), "PE-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin User", "pending.admin@shield.dev", UserRole.ADMIN);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident User", "pending.resident@shield.dev", UserRole.TENANT);
        UserEntity security = createUser(tenant.getId(), unit.getId(), "Security User", "pending.security@shield.dev", UserRole.SECURITY);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);
        String securityToken = login(security.getEmail(), PASSWORD);

        String visitorId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "visitorName", "CSV Guest",
                        "phone", "9888877776",
                        "visitorType", "GUEST"))
                .when()
                .post("/visitors")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String passId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "visitorId", visitorId,
                        "unitId", unit.getId(),
                        "validFrom", Instant.now().minusSeconds(60).toString(),
                        "validTo", Instant.now().plusSeconds(3600).toString(),
                        "visitDate", LocalDate.now().toString(),
                        "purpose", "Testing export"))
                .when()
                .post("/visitor-passes/create")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of("visitorPassId", passId, "entryGate", "MAIN"))
                .when()
                .post("/visitor-logs/entry")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + securityToken)
                .when()
                .get("/visitor-logs/export")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(containsString("text/csv"))
                .body(containsString("visitorPassId"))
                .body(containsString(passId));

        String waterTankId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "tankName", "Overhead Tank",
                        "tankType", "OVERHEAD",
                        "capacity", 10000,
                        "location", "Block A"))
                .when()
                .post("/water-tanks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "tankId", waterTankId,
                        "readingTime", Instant.now().minusSeconds(300).toString(),
                        "levelPercentage", 75.5,
                        "volume", 7550))
                .when()
                .post("/water-level-logs")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "tankId", waterTankId,
                        "readingTime", Instant.now().toString(),
                        "levelPercentage", 74.1,
                        "volume", 7410))
                .when()
                .post("/water-level-logs")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("tankId", waterTankId)
                .queryParam("from", Instant.now().minusSeconds(3600).toString())
                .queryParam("to", Instant.now().plusSeconds(60).toString())
                .queryParam("maxPoints", 1)
                .when()
                .get("/water-level-logs/chart-data")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.tankId", equalTo(waterTankId))
                .body("data.totalPoints", equalTo(1))
                .body("data.points.size()", equalTo(1));

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "name", "Water Supply",
                        "description", "Water-related issues",
                        "slaHours", 24))
                .when()
                .post("/helpdesk-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String ticketId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "categoryId", categoryId,
                        "unitId", unit.getId(),
                        "subject", "No water pressure",
                        "description", "Pressure is very low",
                        "priority", "HIGH"))
                .when()
                .post("/helpdesk-tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String commentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "comment", "Please resolve quickly",
                        "internalNote", false))
                .when()
                .post("/helpdesk-tickets/{id}/comments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", notNullValue())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "comment", "Updating with more details",
                        "internalNote", false))
                .when()
                .put("/ticket-comments/{id}", commentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.comment", equalTo("Updating with more details"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/ticket-comments/{id}", commentId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/{id}/comments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(0));
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
