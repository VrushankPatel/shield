package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AssetComplaintModuleIT extends IntegrationTestBase {

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
    void assetComplaintLifecycleShouldWorkEndToEnd() {
        TenantEntity tenant = createTenant("M6 Asset Society");
        UnitEntity unit = createUnit(tenant.getId(), "AC-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin", "m6.admin@shield.dev", UserRole.ADMIN);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident", "m6.resident@shield.dev", UserRole.TENANT);
        UserEntity assignee = createUser(tenant.getId(), unit.getId(), "Maintainer", "m6.maintainer@shield.dev", UserRole.COMMITTEE);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "categoryName", "Electrical",
                        "description", "Electrical assets"))
                .when()
                .post("/asset-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.categoryName", equalTo("Electrical"))
                .extract()
                .path("data.id");

        String assetId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.ofEntries(
                        Map.entry("assetCode", "A1B"),
                        Map.entry("assetName", "Lobby Light"),
                        Map.entry("categoryId", categoryId),
                        Map.entry("category", "Electrical"),
                        Map.entry("location", "Tower A Lobby"),
                        Map.entry("status", "ACTIVE"),
                        Map.entry("purchaseDate", LocalDate.of(2025, 1, 1).toString()),
                        Map.entry("warrantyExpiryDate", LocalDate.now().plusDays(10).toString()),
                        Map.entry("amcApplicable", true),
                        Map.entry("amcEndDate", LocalDate.now().plusDays(15).toString()),
                        Map.entry("qrCodeData", "QR-A1B")))
                .when()
                .post("/assets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.assetCode", equalTo("A1B"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/assets/category/{categoryId}", categoryId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/assets/verify-qr/{qrCode}", "QR-A1B")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", equalTo(assetId));

        String complaintId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "assetId", assetId,
                        "unitId", unit.getId(),
                        "title", "Lobby light not working",
                        "description", "Main lobby bulb is fused",
                        "priority", "HIGH",
                        "complaintType", "ELECTRICAL",
                        "location", "Tower A Lobby",
                        "slaHours", 4))
                .when()
                .post("/complaints")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("OPEN"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("assignedTo", assignee.getId()))
                .when()
                .post("/complaints/{id}/assign", complaintId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("ASSIGNED"));

        String workOrderId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "complaintId", complaintId,
                        "assetId", assetId,
                        "workDescription", "Replace faulty lobby bulb",
                        "estimatedCost", new BigDecimal("250.00"),
                        "scheduledDate", LocalDate.now().plusDays(1).toString()))
                .when()
                .post("/work-orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/work-orders/{id}/start", workOrderId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("IN_PROGRESS"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/work-orders/{id}/complete", workOrderId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("COMPLETED"))
                .body("data.completionDate", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("resolutionNotes", "Bulb replaced and tested"))
                .when()
                .post("/complaints/{id}/resolve", complaintId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESOLVED"))
                .body("data.resolutionNotes", equalTo("Bulb replaced and tested"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/complaints/{id}/close", complaintId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("CLOSED"));

        String commentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of("comment", "Thanks for quick resolution"))
                .when()
                .post("/complaints/{id}/comments", complaintId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.comment", equalTo("Thanks for quick resolution"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("comment", "Issue acknowledged by resident"))
                .when()
                .put("/comments/{id}", commentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.comment", equalTo("Issue acknowledged by resident"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/complaints/{id}/comments", complaintId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        String scheduleId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "assetId", assetId,
                        "maintenanceType", "Monthly electrical inspection",
                        "frequency", "MONTHLY",
                        "nextMaintenanceDate", LocalDate.now().plusDays(5).toString(),
                        "active", true))
                .when()
                .post("/preventive-maintenance")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.frequency", equalTo("MONTHLY"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/preventive-maintenance/{id}/execute", scheduleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.lastMaintenanceDate", equalTo(LocalDate.now().toString()));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "assetId", assetId,
                        "depreciationMethod", "STRAIGHT_LINE",
                        "depreciationRate", new BigDecimal("10.00"),
                        "depreciationYear", 2026,
                        "baseValue", new BigDecimal("10000.00")))
                .when()
                .post("/asset-depreciation/calculate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.depreciationAmount", equalTo(1000.00f));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/asset-depreciation/asset/{assetId}", assetId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/asset-depreciation/report")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/complaints/statistics")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.total", equalTo(1))
                .body("data.closed", equalTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/assets/warranty-expiring")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/assets/export")
                .then()
                .statusCode(HttpStatus.OK.value());
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
