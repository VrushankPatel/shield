package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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

class Phase2ExpansionFlowsIT extends IntegrationTestBase {

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
    void staffAndPayrollFlowShouldGenerateMonthlyPayrollFromAttendance() {
        TenantEntity tenant = createTenant("Payroll Society");
        UnitEntity unit = createUnit(tenant.getId(), "A-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin Payroll", "admin.payroll@shield.dev", UserRole.ADMIN);

        String adminToken = login(admin.getEmail(), PASSWORD);

        String staffId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "employeeId", "STF-001",
                        "firstName", "Riya",
                        "lastName", "Shah",
                        "phone", "9999999991",
                        "email", "riya.staff@shield.dev",
                        "designation", "SECURITY_GUARD",
                        "dateOfJoining", LocalDate.of(2026, 1, 10).toString(),
                        "employmentType", "FULL_TIME",
                        "basicSalary", 18000,
                        "active", true))
                .when()
                .post("/staff")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.employeeId", equalTo("STF-001"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("staffId", staffId, "attendanceDate", "2026-02-10"))
                .when()
                .post("/staff-attendance/check-in")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PRESENT"))
                .body("data.checkInTime", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("staffId", staffId, "attendanceDate", "2026-02-10"))
                .when()
                .post("/staff-attendance/check-out")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PRESENT"))
                .body("data.checkOutTime", notNullValue());

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/staff-attendance/date/{date}", "2026-02-10")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("from", "2026-02-01")
                .queryParam("to", "2026-02-28")
                .when()
                .get("/staff-attendance/summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalRecords", equalTo(1))
                .body("data.presentCount", equalTo(1));

        String payrollId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "staffId", staffId,
                        "month", 2,
                        "year", 2026,
                        "workingDays", 26,
                        "totalDeductions", 500))
                .when()
                .post("/payroll/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.staffId", equalTo(staffId))
                .body("data.presentDays", equalTo(1))
                .body("data.status", equalTo("DRAFT"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "payrollId", payrollId,
                        "paymentMethod", "BANK_TRANSFER",
                        "paymentReference", "PAY-001",
                        "paymentDate", "2026-02-28",
                        "payslipUrl", "https://files.example/payslip/STF-001-Feb.pdf"))
                .when()
                .post("/payroll/process")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", equalTo(payrollId))
                .body("data.status", equalTo("PROCESSED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/payroll/{id}/approve", payrollId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PAID"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/payroll/staff/{staffId}", staffId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].status", equalTo("PAID"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("month", 2)
                .queryParam("year", 2026)
                .when()
                .get("/payroll/summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalPayrolls", equalTo(1))
                .body("data.netAmount", notNullValue());

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/payroll")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));
    }

    @Test
    void utilityFlowShouldTrackReadingsAndEnforceTenantIsolation() {
        TenantEntity tenantOne = createTenant("Utility Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "B-201");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin Utility", "admin.utility1@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("Utility Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "C-101");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Utility2", "admin.utility2@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String tankId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "tankName", "OH Tank",
                        "tankType", "OVERHEAD",
                        "capacity", 50000,
                        "location", "Block A Terrace"))
                .when()
                .post("/water-tanks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "tankId", tankId,
                        "levelPercentage", 80.50,
                        "volume", 40250,
                        "readingTime", "2026-02-17T10:00:00Z"))
                .when()
                .post("/water-level-logs")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.tankId", equalTo(tankId));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/water-level-logs/tank/{tankId}", tankId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("tankId", tankId)
                .when()
                .get("/water-level-logs/current")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.tankId", equalTo(tankId));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", "2026-02-17T09:00:00Z")
                .queryParam("to", "2026-02-17T12:00:00Z")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/water-level-logs/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        String meterId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "meterNumber", "MTR-001",
                        "meterType", "MAIN",
                        "location", "Transformer Room",
                        "unitId", unitOne.getId()))
                .when()
                .post("/electricity-meters")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "meterId", meterId,
                        "readingDate", "2026-02-17",
                        "readingValue", 10500,
                        "unitsConsumed", 220,
                        "cost", 1760))
                .when()
                .post("/electricity-readings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.meterId", equalTo(meterId));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/electricity-readings/meter/{meterId}", meterId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/electricity-meters/type/{type}", "MAIN")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", "2026-02-01")
                .queryParam("to", "2026-02-28")
                .queryParam("meterId", meterId)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/electricity-readings/date-range")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", "2026-02-01")
                .queryParam("to", "2026-02-28")
                .queryParam("meterId", meterId)
                .when()
                .get("/electricity-readings/consumption-report")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalReadings", equalTo(1))
                .body("data.totalUnitsConsumed", equalTo(220.0f));

        String generatorId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "generatorName", "DG-MAIN-1",
                        "capacityKva", 125.50,
                        "location", "Generator Room"))
                .when()
                .post("/diesel-generators")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.generatorName", equalTo("DG-MAIN-1"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.ofEntries(
                        Map.entry("generatorId", generatorId),
                        Map.entry("logDate", "2026-02-17"),
                        Map.entry("startTime", "2026-02-17T09:30:00Z"),
                        Map.entry("stopTime", "2026-02-17T10:30:00Z"),
                        Map.entry("runtimeHours", 1.0),
                        Map.entry("dieselConsumed", 3.5),
                        Map.entry("dieselCost", 350),
                        Map.entry("meterReadingBefore", 12000),
                        Map.entry("meterReadingAfter", 12025),
                        Map.entry("unitsGenerated", 40)))
                .when()
                .post("/generator-logs")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.generatorId", equalTo(generatorId));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/generator-logs/generator/{generatorId}", generatorId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .queryParam("from", "2026-02-01")
                .queryParam("to", "2026-02-28")
                .queryParam("generatorId", generatorId)
                .when()
                .get("/generator-logs/summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalLogs", equalTo(1))
                .body("data.totalUnitsGenerated", equalTo(40.0f));

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/water-tanks/{id}", tankId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/diesel-generators/{id}", generatorId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void marketplaceFlowShouldHandleInquiryAndListingOwnership() {
        TenantEntity tenant = createTenant("Marketplace Society");
        UnitEntity unit = createUnit(tenant.getId(), "D-401");

        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin Market", "admin.market@shield.dev", UserRole.ADMIN);
        UserEntity seller = createUser(tenant.getId(), unit.getId(), "Seller User", "seller.market@shield.dev", UserRole.TENANT);
        UserEntity buyer = createUser(tenant.getId(), unit.getId(), "Buyer User", "buyer.market@shield.dev", UserRole.OWNER);

        String adminToken = login(admin.getEmail(), PASSWORD);
        String sellerToken = login(seller.getEmail(), PASSWORD);
        String buyerToken = login(buyer.getEmail(), PASSWORD);

        String categoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("categoryName", "Furniture", "description", "Home furniture items"))
                .when()
                .post("/marketplace-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String listingId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + sellerToken)
                .body(Map.of(
                        "categoryId", categoryId,
                        "listingType", "SELL",
                        "title", "Dining Table",
                        "description", "Six seater dining table in good condition",
                        "price", 15000,
                        "negotiable", true,
                        "images", "https://img.example/table.jpg",
                        "unitId", unit.getId()))
                .when()
                .post("/marketplace-listings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.listingNumber", startsWith("MKT-"))
                .body("data.status", equalTo("ACTIVE"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + buyerToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/marketplace-listings/type/{type}", "SELL")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(listingId));

        given()
                .header("Authorization", "Bearer " + buyerToken)
                .queryParam("q", "Dining")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/marketplace-listings/search")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(listingId));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + buyerToken)
                .body(Map.of("message", "Is it available for pickup this weekend?"))
                .when()
                .post("/marketplace-listings/{id}/inquiries", listingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.listingId", equalTo(listingId));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + buyerToken)
                .when()
                .post("/marketplace-listings/{id}/mark-sold", listingId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + sellerToken)
                .when()
                .post("/marketplace-listings/{id}/mark-sold", listingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SOLD"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + buyerToken)
                .when()
                .post("/marketplace-listings/{id}/increment-views", listingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.viewsCount", greaterThan(0));

        given()
                .header("Authorization", "Bearer " + buyerToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/marketplace-inquiries/my-inquiries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].listingId", equalTo(listingId));

        String carpoolId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + sellerToken)
                .body(Map.of(
                        "routeFrom", "Borivali",
                        "routeTo", "BKC",
                        "departureTime", "08:30:00",
                        "availableSeats", 3,
                        "daysOfWeek", "Mon,Tue,Wed",
                        "vehicleType", "CAR",
                        "contactPreference", "PHONE",
                        "active", true))
                .when()
                .post("/carpool-listings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + buyerToken)
                .queryParam("from", "Borivali")
                .queryParam("to", "BKC")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/carpool-listings/route")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(carpoolId));

        given()
                .header("Authorization", "Bearer " + sellerToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/carpool-listings/my-listings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].id", equalTo(carpoolId));
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
