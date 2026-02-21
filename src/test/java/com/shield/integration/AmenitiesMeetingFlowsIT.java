package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AmenitiesMeetingFlowsIT extends IntegrationTestBase {

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
    void amenitiesWorkflowShouldHandleBookingLifecycle() {
        TenantEntity tenant = createTenant("Amenities Society");
        UnitEntity unit = createUnit(tenant.getId(), "A-102");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin Amenities", "admin.amenities@shield.dev", UserRole.ADMIN);
        String token = login(admin.getEmail(), PASSWORD);

        String amenityId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "name", "Banquet Hall",
                        "amenityType", "BANQUET_HALL",
                        "description", "Main hall for events",
                        "capacity", 250,
                        "location", "Clubhouse",
                        "bookingAllowed", true,
                        "advanceBookingDays", 60,
                        "active", true,
                        "requiresApproval", true))
                .when()
                .post("/amenities")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.name", equalTo("Banquet Hall"))
                .extract()
                .path("data.id");

        String timeSlotId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "slotName", "EVENING",
                        "startTime", "17:00:00",
                        "endTime", "22:00:00",
                        "active", true))
                .when()
                .post("/amenities/{id}/time-slots", amenityId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.slotName", equalTo("EVENING"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "timeSlotId", timeSlotId,
                        "dayType", "WEEKEND",
                        "basePrice", 15000,
                        "peakHour", true,
                        "peakHourMultiplier", 1.5))
                .when()
                .post("/amenities/{id}/pricing", amenityId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.dayType", equalTo("WEEKEND"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("ruleType", "MAX_BOOKINGS_PER_MONTH", "ruleValue", "2", "active", true))
                .when()
                .post("/amenities/{id}/rules", amenityId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.ruleType", equalTo("MAX_BOOKINGS_PER_MONTH"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("daysBeforeBooking", 7, "refundPercentage", 80))
                .when()
                .post("/amenities/{id}/cancellation-policy", amenityId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.daysBeforeBooking", equalTo(7));

        String bookingDate = "2026-03-15";
        String startTime = "2026-03-15T17:30:00Z";
        String endTime = "2026-03-15T20:00:00Z";

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("amenityId", amenityId)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .when()
                .get("/amenity-bookings/check-availability")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.available", equalTo(true));

        String bookingId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "timeSlotId", timeSlotId,
                        "startTime", startTime,
                        "endTime", endTime,
                        "bookingDate", bookingDate,
                        "numberOfPersons", 120,
                        "purpose", "Reception",
                        "bookingAmount", 15000,
                        "securityDeposit", 5000,
                        "notes", "Wedding reception"))
                .when()
                .post("/amenities/{id}/book", amenityId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.bookingNumber", startsWith("ABK-"))
                .body("data.status", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/amenity-bookings/{id}/approve", bookingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("CONFIRMED"))
                .body("data.approvalDate", notNullValue());

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/amenity-bookings/my-bookings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(bookingId));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/amenity-bookings/{id}/complete", bookingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("COMPLETED"));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/amenity-bookings/date/{date}", bookingDate)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));
    }

    @Test
    void meetingWorkflowShouldHandleGovernanceLifecycle() {
        TenantEntity tenant = createTenant("Meeting Society");
        UnitEntity unit = createUnit(tenant.getId(), "B-202");

        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin Meeting", "admin.meeting@shield.dev", UserRole.ADMIN);
        UserEntity member = createUser(tenant.getId(), unit.getId(), "Member One", "member.meeting@shield.dev", UserRole.OWNER);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String memberToken = login(member.getEmail(), PASSWORD);

        String meetingId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "meetingType", "AGM",
                        "title", "Annual General Meeting",
                        "agenda", "Budget and maintenance review",
                        "scheduledAt", "2026-03-20T10:00:00Z",
                        "location", "Community Hall",
                        "meetingMode", "IN_PERSON"))
                .when()
                .post("/meetings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SCHEDULED"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "agendaItem", "Budget discussion",
                        "description", "Review annual budget",
                        "displayOrder", 1,
                        "presenter", admin.getId(),
                        "estimatedDuration", 30))
                .when()
                .post("/meetings/{id}/agenda", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.displayOrder", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("userId", member.getId()))
                .when()
                .post("/meetings/{id}/attendees", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.userId", equalTo(member.getId().toString()))
                .body("data.rsvpStatus", equalTo("PENDING"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + memberToken)
                .body(Map.of("rsvpStatus", "ACCEPTED"))
                .when()
                .post("/meetings/{id}/rsvp", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.rsvpStatus", equalTo("ACCEPTED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/meetings/{id}/start", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("ONGOING"));

        String minutesId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "minutesContent", "AGM minutes content",
                        "summary", "Budget approved for next year",
                        "preparedBy", admin.getId(),
                        "documentUrl", "https://files.example/meeting/agm-minutes.pdf"))
                .when()
                .post("/meetings/{id}/minutes", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.summary", equalTo("Budget approved for next year"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("approvedBy", admin.getId()))
                .when()
                .post("/minutes/{id}/approve", minutesId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.approvedBy", equalTo(admin.getId().toString()));

        String resolutionId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "resolutionText", "Increase security staffing",
                        "proposedBy", admin.getId(),
                        "secondedBy", member.getId()))
                .when()
                .post("/meetings/{id}/resolutions", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.resolutionNumber", startsWith("RES-"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + memberToken)
                .body(Map.of("vote", "FOR"))
                .when()
                .post("/resolutions/{id}/vote", resolutionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.vote", equalTo("FOR"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/resolutions/{id}/results", resolutionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.votesFor", equalTo(1))
                .body("data.totalVotes", equalTo(1));

        String actionItemId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "actionDescription", "Finalize security vendor contract",
                        "assignedTo", member.getId(),
                        "dueDate", "2026-03-25",
                        "priority", "HIGH",
                        "status", "PENDING"))
                .when()
                .post("/meetings/{id}/action-items", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.assignedTo", equalTo(member.getId().toString()))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + memberToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/action-items/assigned-to-me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/action-items/{id}/complete", actionItemId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("COMPLETED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("reminderType", "24_HOURS")
                .when()
                .post("/meetings/{id}/send-reminders", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reminderType", equalTo("24_HOURS"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/meetings/{id}/reminders", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/meetings/{id}/end", meetingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("COMPLETED"));
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
        unit.setSquareFeet(BigDecimal.valueOf(1200));
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
