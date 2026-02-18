package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

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

class VisitorManagementModuleIT extends IntegrationTestBase {

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
    void visitorManagementFlowShouldWorkEndToEnd() {
        TenantEntity tenant = createTenant("M5 Visitor Society");
        UnitEntity unit = createUnit(tenant.getId(), "V-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin User", "m5.admin@shield.dev", UserRole.ADMIN);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident User", "m5.resident@shield.dev", UserRole.TENANT);
        UserEntity security = createUser(tenant.getId(), unit.getId(), "Security User", "m5.security@shield.dev", UserRole.SECURITY);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);
        String securityToken = login(security.getEmail(), PASSWORD);

        String visitorId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "visitorName", "Courier Partner",
                        "phone", "9988776655",
                        "visitorType", "DELIVERY",
                        "vehicleNumber", "MH01AA1111"))
                .when()
                .post("/visitors")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.visitorName", equalTo("Courier Partner"))
                .extract()
                .path("data.id");

        String passId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "visitorId", visitorId,
                        "unitId", unit.getId(),
                        "validFrom", Instant.now().plusSeconds(60).toString(),
                        "validTo", Instant.now().plusSeconds(7200).toString(),
                        "visitDate", LocalDate.now().toString(),
                        "purpose", "Food delivery",
                        "numberOfPersons", 1))
                .when()
                .post("/visitor-passes/create")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"))
                .body("data.passNumber", startsWith("VP-"))
                .extract()
                .path("data.id");

        String qrCode = given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/visitor-passes/{id}", passId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.qrCode");

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/visitor-passes/verify/{qrCode}", qrCode)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", equalTo(passId));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of(
                        "visitorPassId", passId,
                        "entryGate", "MAIN_GATE"))
                .when()
                .post("/visitor-logs/entry")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.entryTime", notNullValue());

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/visitor-passes/active")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/visitor-logs/currently-inside")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of(
                        "visitorPassId", passId,
                        "exitGate", "MAIN_GATE"))
                .when()
                .post("/visitor-logs/exit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.exitTime", notNullValue());

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/visitor-logs/currently-inside")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(0));

        String legacyPassId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "visitorName", "Legacy Guest",
                        "vehicleNumber", "MH02BB2222",
                        "validFrom", Instant.now().toString(),
                        "validTo", Instant.now().plusSeconds(5400).toString()))
                .when()
                .post("/visitors/pass")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/visitors/pass/{id}/approve", legacyPassId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("APPROVED"));

        String helpId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of(
                        "helpName", "Maid Sita",
                        "phone", "9000000001",
                        "helpType", "MAID",
                        "permanentPass", true))
                .when()
                .post("/domestic-help")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.helpName", equalTo("Maid Sita"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "startDate", LocalDate.now().toString()))
                .when()
                .post("/domestic-help/{id}/assign-unit", helpId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true));

        given()
                .header("Authorization", "Bearer " + securityToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/domestic-help/unit/{unitId}", unit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + securityToken)
                .when()
                .post("/domestic-help/{id}/verify", helpId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.policeVerificationDone", equalTo(true));

        String blacklistId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "personName", "Blocked Person",
                        "phone", "9111111111",
                        "reason", "Repeated entry violations"))
                .when()
                .post("/blacklist")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/blacklist/check/{phone}", "9111111111")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.blacklisted", equalTo(true));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/blacklist/{id}/deactivate", blacklistId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(false));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/blacklist/check/{phone}", "9111111111")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.blacklisted", equalTo(false));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/blacklist/{id}/activate", blacklistId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + securityToken)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "deliveryPartner", "AMAZON",
                        "trackingNumber", "TRK-001",
                        "deliveryTime", Instant.now().toString(),
                        "receivedBy", resident.getId()))
                .when()
                .post("/delivery-logs")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.deliveryPartner", equalTo("AMAZON"));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/delivery-logs/unit/{unitId}", unit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("from", Instant.now().minusSeconds(7200).toString())
                .queryParam("to", Instant.now().plusSeconds(7200).toString())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/delivery-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void visitorResourcesShouldBeTenantIsolated() {
        TenantEntity tenantOne = createTenant("M5 Isolation One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "A-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "m5.one@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("M5 Isolation Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "B-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "m5.two@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String visitorId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "visitorName", "Private Visitor",
                        "phone", "9000012345"))
                .when()
                .post("/visitors")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/visitors/{id}", visitorId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        String passId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "visitorId", visitorId,
                        "unitId", unitOne.getId(),
                        "validFrom", Instant.now().toString(),
                        "validTo", Instant.now().plusSeconds(1800).toString(),
                        "visitDate", LocalDate.now().toString(),
                        "purpose", "Private meeting"))
                .when()
                .post("/visitor-passes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/visitor-passes/{id}", passId)
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
