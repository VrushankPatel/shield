package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

class M9CompletionFlowsIT extends IntegrationTestBase {

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
    void helpdeskLifecycleShouldSupportCloseRateStatsAndAttachments() {
        TenantEntity tenant = createTenant("M9 Helpdesk");
        UnitEntity unit = createUnit(tenant.getId(), "M9-H-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin", "m9.helpdesk.admin@shield.dev", UserRole.ADMIN);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident", "m9.helpdesk.resident@shield.dev", UserRole.TENANT);
        UserEntity assignee = createUser(tenant.getId(), unit.getId(), "Assignee", "m9.helpdesk.assignee@shield.dev", UserRole.COMMITTEE);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("name", "Electrical", "description", "Lights and wiring", "slaHours", 4))
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
                        "subject", "Lobby light not working",
                        "description", "Main lobby light is off since morning",
                        "priority", "HIGH"))
                .when()
                .post("/helpdesk-tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("OPEN"))
                .extract()
                .path("data.id");

        String attachmentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of("fileName", "photo.jpg", "fileUrl", "https://files.shield.dev/photo.jpg"))
                .when()
                .post("/helpdesk-tickets/{id}/attachments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.fileName", equalTo("photo.jpg"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/{id}/attachments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("assignedTo", assignee.getId()))
                .when()
                .post("/helpdesk-tickets/{id}/assign", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("IN_PROGRESS"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("resolutionNotes", "Replaced fuse and restored power"))
                .when()
                .post("/helpdesk-tickets/{id}/resolve", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESOLVED"));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .post("/helpdesk-tickets/{id}/close", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("CLOSED"))
                .body("data.closedAt", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of("satisfactionRating", 5))
                .when()
                .post("/helpdesk-tickets/{id}/rate", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.satisfactionRating", equalTo(5));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/my-tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/assigned-to-me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(0));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/status/CLOSED")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/helpdesk-tickets/statistics")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalTickets", equalTo(1))
                .body("data.closedTickets", equalTo(1))
                .body("data.averageSatisfactionRating", equalTo(5.00f));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/ticket-attachments/{id}", attachmentId)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void emergencyModuleShouldSupportSafetyAndDateRangeEndpoints() {
        TenantEntity tenant = createTenant("M9 Emergency");
        UnitEntity unit = createUnit(tenant.getId(), "M9-E-101");
        UserEntity committee = createUser(tenant.getId(), unit.getId(), "Committee", "m9.emergency.committee@shield.dev", UserRole.COMMITTEE);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident", "m9.emergency.resident@shield.dev", UserRole.TENANT);
        UserEntity security = createUser(tenant.getId(), unit.getId(), "Security", "m9.emergency.security@shield.dev", UserRole.SECURITY);

        String committeeToken = login(committee.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);
        String securityToken = login(security.getEmail(), PASSWORD);

        String contactId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of(
                        "contactType", "FIRE",
                        "contactName", "City Fire Dept",
                        "phonePrimary", "100",
                        "phoneSecondary", "101",
                        "address", "Main Road",
                        "displayOrder", 3,
                        "active", true))
                .when()
                .post("/emergency-contacts")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/emergency-contacts/type/FIRE")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of("displayOrder", 1))
                .when()
                .patch("/emergency-contacts/{id}/order", contactId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.displayOrder", equalTo(1));

        String alertId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "alertType", "FIRE",
                        "location", "Tower Gate",
                        "description", "Smoke detected near generator room",
                        "latitude", BigDecimal.valueOf(19.11223344),
                        "longitude", BigDecimal.valueOf(72.55667788)))
                .when()
                .post("/sos-alerts/raise")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.alertNumber", startsWith("SOS-"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + securityToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/sos-alerts/type/FIRE")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now().plusSeconds(3600);
        given()
                .header("Authorization", "Bearer " + securityToken)
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/sos-alerts/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + securityToken)
                .when()
                .post("/sos-alerts/{id}/mark-false-alarm", alertId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("FALSE_ALARM"));

        String fireDrillId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of(
                        "drillDate", LocalDate.now().toString(),
                        "drillTime", "10:30:00",
                        "conductedBy", committee.getId(),
                        "evacuationTime", 180,
                        "participantsCount", 56,
                        "observations", "Good response time",
                        "reportUrl", "https://files.shield.dev/fire-drill-report.pdf"))
                .when()
                .post("/fire-drill-records")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/fire-drill-records/year/{year}", LocalDate.now().getYear())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(fireDrillId));

        String equipmentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of(
                        "equipmentType", "FIRE_EXTINGUISHER",
                        "equipmentTag", "FE-01",
                        "location", "Block A lobby",
                        "installationDate", LocalDate.now().minusMonths(6).toString(),
                        "lastInspectionDate", LocalDate.now().minusMonths(1).toString(),
                        "nextInspectionDate", LocalDate.now().toString(),
                        "inspectionFrequencyDays", 30,
                        "functional", true))
                .when()
                .post("/safety-equipment")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/safety-equipment/type/FIRE_EXTINGUISHER")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/safety-equipment/inspection-due")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of(
                        "equipmentId", equipmentId,
                        "inspectionDate", LocalDate.now().toString(),
                        "inspectedBy", committee.getId(),
                        "inspectionResult", "PASSED",
                        "remarks", "Pressure and seal checked"))
                .when()
                .post("/safety-inspections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.equipmentId", equalTo(equipmentId))
                .body("data.inspectionResult", equalTo("PASSED"));

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/safety-inspections/equipment/{equipmentId}", equipmentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));
    }

    @Test
    void documentModuleShouldSupportSearchExpiringAndAccessLogs() {
        TenantEntity tenant = createTenant("M9 Documents");
        UnitEntity unit = createUnit(tenant.getId(), "M9-D-101");
        UserEntity committee = createUser(tenant.getId(), unit.getId(), "Committee", "m9.document.committee@shield.dev", UserRole.COMMITTEE);
        String token = login(committee.getEmail(), PASSWORD);

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "categoryName", "BYLAWS",
                        "description", "Bylaws and policy docs"))
                .when()
                .post("/document-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "categoryName", "AGM",
                        "description", "AGM specific documents",
                        "parentCategoryId", categoryId))
                .when()
                .post("/document-categories")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/document-categories/hierarchy")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(1))
                .body("data[0].id", equalTo(categoryId))
                .body("data[0].children.size()", equalTo(1));

        LocalDate expiry = LocalDate.now().plusDays(15);
        String documentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "documentName", "Society Bylaws 2026",
                        "categoryId", categoryId,
                        "documentType", "PDF",
                        "fileUrl", "https://files.shield.dev/bylaws-2026.pdf",
                        "fileSize", 2048,
                        "description", "Official bylaws document",
                        "versionLabel", "v1",
                        "publicAccess", true,
                        "expiryDate", expiry.toString(),
                        "tags", "policy,bylaws"))
                .when()
                .post("/documents/upload")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/documents/{id}", documentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", equalTo(documentId));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/documents/{id}/download", documentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.documentId", equalTo(documentId))
                .body("data.fileUrl", startsWith("https://"));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("q", "bylaws")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/documents/search")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/documents/tags/policy")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("from", LocalDate.now().toString())
                .queryParam("to", LocalDate.now().plusDays(30).toString())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/documents/expiring")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/documents/{id}/access-logs", documentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(2));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/document-access-logs/user/{userId}", committee.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(2));

        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now().plusSeconds(3600);
        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/document-access-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(2));
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
