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
                .body("data.status", equalTo("DRAFT"));

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
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/water-tanks/{id}", tankId)
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
