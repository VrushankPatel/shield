package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class RealLifeFlowsIT extends IntegrationTestBase {

    private static final String PASSWORD = "password123";

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void announcementPublishFlowShouldRespectAudienceAndNotificationPreferences() {
        TenantEntity tenant = createTenant("E2E Announcements");
        UnitEntity unit = createUnit(tenant.getId(), "A-101");

        UserEntity committee = createUser(tenant.getId(), unit.getId(), "Committee User", "committee@shield.dev", UserRole.COMMITTEE);
        UserEntity tenantOptOut = createUser(tenant.getId(), unit.getId(), "Resident OptOut", "resident.optout@shield.dev", UserRole.TENANT);
        createUser(tenant.getId(), unit.getId(), "Resident Default", "resident.default@shield.dev", UserRole.TENANT);
        createUser(tenant.getId(), unit.getId(), "Owner User", "owner@shield.dev", UserRole.OWNER);

        String committeeToken = login(committee.getEmail(), PASSWORD);
        String tenantOptOutToken = login(tenantOptOut.getEmail(), PASSWORD);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tenantOptOutToken)
                .body(Map.of("emailEnabled", false))
                .when()
                .put("/notification-preferences")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.emailEnabled", equalTo(false));

        String announcementId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .body(Map.of(
                        "title", "Water Supply Shutdown",
                        "content", "Water supply will be unavailable from 2PM to 5PM for pipeline maintenance.",
                        "category", "MAINTENANCE",
                        "priority", "HIGH",
                        "emergency", false,
                        "targetAudience", "TENANTS"))
                .when()
                .post("/announcements")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("DRAFT"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + committeeToken)
                .when()
                .post("/announcements/{id}/publish", announcementId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.announcement.status", equalTo("PUBLISHED"))
                .body("data.notificationDispatch.total", equalTo(2))
                .body("data.notificationDispatch.sent", equalTo(0))
                .body("data.notificationDispatch.failed", equalTo(0))
                .body("data.notificationDispatch.skipped", equalTo(2));

        given()
                .header("Authorization", "Bearer " + committeeToken)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/notifications")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(2))
                .body("data.content.recipientEmail", hasItems("resident.optout@shield.dev", "resident.default@shield.dev"))
                .body("data.content.recipientEmail", not(hasItem("owner@shield.dev")))
                .body("data.content.status", everyItem(equalTo("SKIPPED")))
                .body("data.content.sourceType", everyItem(equalTo("ANNOUNCEMENT")));
    }

    @Test
    void helpdeskIncidentFlowShouldMoveFromOpenToResolved() {
        TenantEntity tenant = createTenant("E2E Helpdesk");
        UnitEntity unit = createUnit(tenant.getId(), "B-204");

        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin User", "admin.helpdesk@shield.dev", UserRole.ADMIN);
        UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident User", "resident.helpdesk@shield.dev", UserRole.TENANT);
        UserEntity assignee = createUser(tenant.getId(), unit.getId(), "Ops User", "ops.helpdesk@shield.dev", UserRole.COMMITTEE);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String residentToken = login(resident.getEmail(), PASSWORD);
        String assigneeToken = login(assignee.getEmail(), PASSWORD);

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "name", "Plumbing",
                        "description", "Leaks, pipe burst and drainage issues",
                        "slaHours", 6))
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
                        "subject", "Water leakage near lobby lift",
                        "description", "Pipe burst caused water spread in common area.",
                        "priority", "HIGH"))
                .when()
                .post("/helpdesk-tickets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("OPEN"))
                .body("data.ticketNumber", startsWith("HD-"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of("assignedTo", assignee.getId()))
                .when()
                .post("/helpdesk-tickets/{id}/assign", ticketId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("assignedTo", assignee.getId()))
                .when()
                .post("/helpdesk-tickets/{id}/assign", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("IN_PROGRESS"))
                .body("data.assignedTo", equalTo(assignee.getId().toString()))
                .body("data.assignedAt", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + assigneeToken)
                .body(Map.of("comment", "Plumber dispatched. Valve isolated to stop leakage.", "internalNote", false))
                .when()
                .post("/helpdesk-tickets/{id}/comments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.userId", equalTo(assignee.getId().toString()))
                .body("data.comment", containsString("Plumber dispatched"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("resolutionNotes", "Faulty pipe section replaced and pressure tested."))
                .when()
                .post("/helpdesk-tickets/{id}/resolve", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESOLVED"))
                .body("data.resolutionNotes", containsString("pressure tested"))
                .body("data.resolvedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/helpdesk-tickets/{id}", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESOLVED"))
                .body("data.assignedTo", equalTo(assignee.getId().toString()));

        given()
                .header("Authorization", "Bearer " + residentToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/helpdesk-tickets/{id}/comments", ticketId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].comment", containsString("Plumber dispatched"));
    }

    @Test
    void sosLifecycleFlowShouldTrackIncidentAndEnforceTenantIsolation() {
        TenantEntity tenantOne = createTenant("E2E Emergency One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "C-301");
        UserEntity resident = createUser(tenantOne.getId(), unitOne.getId(), "Resident SOS", "resident.sos@shield.dev", UserRole.TENANT);
        UserEntity responder = createUser(tenantOne.getId(), unitOne.getId(), "Security SOS", "security.sos@shield.dev", UserRole.SECURITY);

        TenantEntity tenantTwo = createTenant("E2E Emergency Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "D-101");
        UserEntity outsider = createUser(tenantTwo.getId(), unitTwo.getId(), "Outsider", "outsider.sos@shield.dev", UserRole.TENANT);

        String residentToken = login(resident.getEmail(), PASSWORD);
        String responderToken = login(responder.getEmail(), PASSWORD);
        String outsiderToken = login(outsider.getEmail(), PASSWORD);

        String alertId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "alertType", "MEDICAL",
                        "location", "Tower C Lobby",
                        "description", "Resident collapsed near security desk.",
                        "latitude", BigDecimal.valueOf(19.12345678),
                        "longitude", BigDecimal.valueOf(72.87654321)))
                .when()
                .post("/sos-alerts/raise")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("ACTIVE"))
                .body("data.alertNumber", startsWith("SOS-"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + responderToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/sos-alerts/active")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(alertId))
                .body("data.content[0].status", equalTo("ACTIVE"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + responderToken)
                .body(Map.of("respondedBy", responder.getId()))
                .when()
                .post("/sos-alerts/{id}/respond", alertId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESPONDED"))
                .body("data.respondedBy", equalTo(responder.getId().toString()))
                .body("data.respondedAt", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + responderToken)
                .body(Map.of("falseAlarm", false))
                .when()
                .post("/sos-alerts/{id}/resolve", alertId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("RESOLVED"))
                .body("data.resolvedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + responderToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/sos-alerts/active")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(0));

        given()
                .header("Authorization", "Bearer " + outsiderToken)
                .when()
                .get("/sos-alerts/{id}", alertId)
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
