package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class ConfigAndFilesFlowsIT extends IntegrationTestBase {

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
    void configAndSettingsFlowShouldSupportGovernanceAndTenantIsolation() {
        TenantEntity tenantOne = createTenant("Config Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "E-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.config.one@shield.dev", UserRole.ADMIN);
        UserEntity committeeOne = createUser(tenantOne.getId(), unitOne.getId(), "Committee One", "committee.config.one@shield.dev", UserRole.COMMITTEE);

        TenantEntity tenantTwo = createTenant("Config Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "F-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.config.two@shield.dev", UserRole.ADMIN);

        String adminTokenOne = login(adminOne.getEmail(), PASSWORD);
        String committeeTokenOne = login(committeeOne.getEmail(), PASSWORD);
        String adminTokenTwo = login(adminTwo.getEmail(), PASSWORD);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminTokenOne)
                .body(Map.of(
                        "value", "15",
                        "category", "security"))
                .when()
                .put("/config/{key}", "visitor.daily.limit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.key", equalTo("visitor.daily.limit"))
                .body("data.value", equalTo("15"));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/config/{key}", "visitor.daily.limit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.category", equalTo("security"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminTokenOne)
                .body(Map.of(
                        "entries", List.of(
                                Map.of("key", "amenity.max.bookings", "value", "3", "category", "amenities"),
                                Map.of("key", "parking.visitor.allowed", "value", "true", "category", "security"))))
                .when()
                .post("/config/bulk-update")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(2));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/config/category/{category}", "security")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(2));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeTokenOne)
                .body(Map.of("enabled", false))
                .when()
                .put("/settings/modules/{module}/toggle", "marketplace")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminTokenOne)
                .body(Map.of("enabled", false))
                .when()
                .put("/settings/modules/{module}/toggle", "marketplace")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.module", equalTo("marketplace"))
                .body("data.enabled", equalTo(false));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/settings/modules")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.module", hasItem("marketplace"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminTokenOne)
                .body(Map.of("value", Map.of("method", "HYBRID", "fixedShare", 0.40)))
                .when()
                .put("/settings/billing-formula")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.value.method", equalTo("HYBRID"));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/settings/billing-formula")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.value.method", equalTo("HYBRID"));

        given()
                .header("Authorization", "Bearer " + adminTokenTwo)
                .when()
                .get("/config/{key}", "visitor.daily.limit")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void fileFlowShouldUploadDownloadDeleteAndEnforceTenantIsolation() {
        TenantEntity tenantOne = createTenant("File Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "G-701");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin File", "admin.file.one@shield.dev", UserRole.ADMIN);
        UserEntity residentOne = createUser(tenantOne.getId(), unitOne.getId(), "Resident File", "resident.file.one@shield.dev", UserRole.TENANT);

        TenantEntity tenantTwo = createTenant("File Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "H-102");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.file.two@shield.dev", UserRole.ADMIN);

        String adminTokenOne = login(adminOne.getEmail(), PASSWORD);
        String residentTokenOne = login(residentOne.getEmail(), PASSWORD);
        String adminTokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String fileId = given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .multiPart("file", "gate-pass.txt", "Gate pass for cab number MH01AB1234".getBytes(StandardCharsets.UTF_8), "text/plain")
                .when()
                .post("/files/upload")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", notNullValue())
                .body("data.status", equalTo("ACTIVE"))
                .extract()
                .path("data.fileId");

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/files/{fileId}", fileId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.fileName", equalTo("gate-pass.txt"));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/files/{fileId}/download", fileId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("Gate pass for cab number MH01AB1234"));

        given()
                .header("Authorization", "Bearer " + residentTokenOne)
                .when()
                .delete("/files/{fileId}", fileId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .multiPart("files", "notice-1.txt", "Maintenance notice".getBytes(StandardCharsets.UTF_8), "text/plain")
                .multiPart("files", "notice-2.txt", "Fire drill notice".getBytes(StandardCharsets.UTF_8), "text/plain")
                .when()
                .post("/files/upload-multiple")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(2));

        String reservedFileId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminTokenOne)
                .body(Map.of(
                        "fileName", "statement.pdf",
                        "contentType", "application/pdf",
                        "expiresInMinutes", 20))
                .when()
                .post("/files/generate-presigned-url")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.uploadUrl", containsString("/api/v1/files/upload"))
                .extract()
                .path("data.fileId");

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .queryParam("fileId", reservedFileId)
                .multiPart("file", "statement.pdf", "Mock PDF bytes".getBytes(StandardCharsets.UTF_8), "application/pdf")
                .when()
                .post("/files/upload")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.fileId", equalTo(reservedFileId));

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .delete("/files/{fileId}", fileId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + adminTokenOne)
                .when()
                .get("/files/{fileId}", fileId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        given()
                .header("Authorization", "Bearer " + adminTokenTwo)
                .when()
                .get("/files/{fileId}", reservedFileId)
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
