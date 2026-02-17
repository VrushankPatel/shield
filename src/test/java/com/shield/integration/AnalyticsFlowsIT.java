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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AnalyticsFlowsIT extends IntegrationTestBase {

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
    void analyticsGovernanceFlowShouldAggregateRealOperationalEvents() {
        TenantEntity tenantOne = createTenant("Analytics Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "A-301");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.analytics1@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("Analytics Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "B-401");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.analytics2@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String billId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "month", 2,
                        "year", 2026,
                        "amount", 1000,
                        "dueDate", "2026-02-28",
                        "lateFee", 50))
                .when()
                .post("/billing/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "billId", billId,
                        "amount", 700,
                        "mode", "UPI",
                        "transactionRef", "TXN-AN-700"))
                .when()
                .post("/payments")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.billId", equalTo(billId));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "type", "EXPENSE",
                        "category", "MAINTENANCE",
                        "amount", 300,
                        "reference", "EXP-1",
                        "description", "Pipe replacement",
                        "entryDate", "2026-02-17"))
                .when()
                .post("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "type", "EXPENSE",
                        "category", "SECURITY",
                        "amount", 200,
                        "reference", "EXP-2",
                        "description", "Night shift allowance",
                        "entryDate", "2026-02-17"))
                .when()
                .post("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "visitorName", "Courier Partner",
                        "vehicleNumber", "MH01AB1234",
                        "validFrom", "2026-02-17T09:00:00Z",
                        "validTo", "2026-02-17T12:00:00Z"))
                .when()
                .post("/visitors/pass")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"));

        String staffId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "employeeId", "AN-STF-01",
                        "firstName", "Rohit",
                        "lastName", "Guard",
                        "phone", "9999999988",
                        "email", "rohit.guard@shield.dev",
                        "designation", "SECURITY_GUARD",
                        "dateOfJoining", "2026-01-01",
                        "employmentType", "FULL_TIME",
                        "basicSalary", 18000,
                        "active", true))
                .when()
                .post("/staff")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String attendanceDate = LocalDate.now().toString();
        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "staffId", staffId,
                        "attendanceDate", attendanceDate))
                .when()
                .post("/staff-attendance/check-in")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PRESENT"));

        String templateId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "templateName", "Monthly Collection KPI",
                        "reportType", "COLLECTION_EFFICIENCY",
                        "description", "Tracks billed vs collected amount",
                        "queryTemplate", "",
                        "parametersJson", "{}",
                        "systemTemplate", false))
                .when()
                .post("/report-templates")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .post("/report-templates/{id}/execute", templateId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("COLLECTION_EFFICIENCY"))
                .body("data.data.collectionEfficiency.collectionEfficiencyPercent", equalTo(70.0f));

        String scheduledReportId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "templateId", templateId,
                        "reportName", "Nightly KPI Mail",
                        "frequency", "DAILY",
                        "recipients", "committee@shield.dev",
                        "active", true))
                .when()
                .post("/scheduled-reports")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .post("/scheduled-reports/{id}/send-now", scheduledReportId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.lastGeneratedAt", notNullValue())
                .body("data.nextGenerationAt", notNullValue());

        String dashboardId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "dashboardName", "Committee Command Center",
                        "dashboardType", "COMMITTEE",
                        "widgetsJson", "{\"widgets\":[\"collection\",\"expenses\"]}",
                        "defaultDashboard", true))
                .when()
                .post("/analytics-dashboards")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.defaultDashboard", equalTo(true))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/analytics/collection-efficiency")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.billedAmount", equalTo(1000.0f))
                .body("data.collectedAmount", equalTo(700.0f))
                .body("data.collectionEfficiencyPercent", equalTo(70.0f));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/analytics/expense-distribution")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(2));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/analytics/staff-attendance-summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalStaff", equalTo(1))
                .body("data.presentToday", equalTo(1))
                .body("data.absentToday", equalTo(0));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/analytics/visitor-trends")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", greaterThanOrEqualTo(1));

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/report-templates/{id}", templateId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/analytics-dashboards/{id}", dashboardId)
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
