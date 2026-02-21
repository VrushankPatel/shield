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

class AccountingTreasuryModuleIT extends IntegrationTestBase {

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
    void accountingTreasuryFlowShouldWorkEndToEnd() {
        TenantEntity tenant = createTenant("M4 Treasury Society");
        UnitEntity unit = createUnit(tenant.getId(), "T-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Treasurer", "treasurer@shield.dev", UserRole.ADMIN);

        String token = login(admin.getEmail(), PASSWORD);

        String expenseHeadId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "headName", "Maintenance Expense",
                        "headType", "EXPENSE"))
                .when()
                .post("/account-heads")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.headType", equalTo("EXPENSE"))
                .extract()
                .path("data.id");

        String incomeHeadId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "headName", "Maintenance Collection",
                        "headType", "INCOME"))
                .when()
                .post("/account-heads")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.headType", equalTo("INCOME"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/account-heads/hierarchy")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", greaterThanOrEqualTo(2));

        String fundCategoryId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "categoryName", "Reserve Fund",
                        "description", "Emergency reserve",
                        "currentBalance", 50000))
                .when()
                .post("/fund-categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.currentBalance", equalTo(50000))
                .extract()
                .path("data.id");

        String vendorId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "vendorName", "ABC Electricals",
                        "contactPerson", "Raj",
                        "phone", "9999999999",
                        "email", "vendor@abc.com",
                        "vendorType", "ELECTRICAL"))
                .when()
                .post("/vendors")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true))
                .extract()
                .path("data.id");

        String financialYear = LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "financialYear", financialYear,
                        "accountHeadId", expenseHeadId,
                        "budgetedAmount", 200000))
                .when()
                .post("/budgets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.financialYear", equalTo(financialYear));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "entryDate", LocalDate.now().toString(),
                        "accountHeadId", incomeHeadId,
                        "fundCategoryId", fundCategoryId,
                        "transactionType", "CREDIT",
                        "amount", 100000,
                        "description", "Monthly collection"))
                .when()
                .post("/ledger-entries")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.type", equalTo("INCOME"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/ledger-entries/account/{accountHeadId}", incomeHeadId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        String expenseId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "accountHeadId", expenseHeadId,
                        "fundCategoryId", fundCategoryId,
                        "vendorId", vendorId,
                        "expenseDate", LocalDate.now().toString(),
                        "amount", 12000,
                        "description", "Lift repair"))
                .when()
                .post("/expenses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.paymentStatus", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/expenses/pending-approval")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/expenses/{id}/approve", expenseId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.paymentStatus", equalTo("PAID"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "vendorId", vendorId,
                        "expenseId", expenseId,
                        "paymentDate", LocalDate.now().toString(),
                        "amount", 12000,
                        "paymentMethod", "NEFT",
                        "transactionReference", "NEFT-1234",
                        "status", "COMPLETED"))
                .when()
                .post("/vendor-payments")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("COMPLETED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "vendorId", vendorId,
                        "paymentDate", LocalDate.now().toString(),
                        "amount", 5000,
                        "paymentMethod", "CHEQUE",
                        "status", "PENDING"))
                .when()
                .post("/vendor-payments")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/vendor-payments/pending")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("financialYear", financialYear)
                .when()
                .get("/budgets/vs-actual")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", hasSize(1))
                .body("data[0].actualAmount", equalTo(12000.0f));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("financialYear", financialYear)
                .when()
                .get("/reports/income-statement")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("INCOME_STATEMENT"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reports/balance-sheet")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("BALANCE_SHEET"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reports/cash-flow")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("CASH_FLOW"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reports/trial-balance")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("TRIAL_BALANCE"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/reports/fund-summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.reportType", equalTo("FUND_SUMMARY"));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("financialYear", financialYear)
                .when()
                .get("/reports/export/ca-format")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", notNullValue());
    }

    @Test
    void accountingResourcesShouldBeTenantIsolated() {
        TenantEntity tenantOne = createTenant("M4 Isolation One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "A-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "m4.one@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("M4 Isolation Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "B-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "m4.two@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String accountHeadId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of("headName", "Private Head", "headType", "EXPENSE"))
                .when()
                .post("/account-heads")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/account-heads/{id}", accountHeadId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void ledgerEndpointsShouldBeTenantScopedForListAndSummary() {
        TenantEntity tenantOne = createTenant("M4 Ledger One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "L-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Ledger One", "m4.ledger1@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("M4 Ledger Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "L-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Ledger Two", "m4.ledger2@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "type", "INCOME",
                        "category", "MAINTENANCE",
                        "amount", 3000,
                        "reference", "LED-INC-1",
                        "description", "Maintenance collections",
                        "entryDate", "2026-02-21"))
                .when()
                .post("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "type", "EXPENSE",
                        "category", "REPAIR",
                        "amount", 1200,
                        "reference", "LED-EXP-1",
                        "description", "Motor repair",
                        "entryDate", "2026-02-21"))
                .when()
                .post("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenTwo)
                .body(Map.of(
                        "type", "INCOME",
                        "category", "PARKING",
                        "amount", 9999,
                        "reference", "LED-INC-2",
                        "description", "Parking collections",
                        "entryDate", "2026-02-21"))
                .when()
                .post("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(2))
                .body("data.content[0].tenantId", equalTo(tenantOne.getId().toString()))
                .body("data.content[1].tenantId", equalTo(tenantOne.getId().toString()));

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/ledger")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content", hasSize(1))
                .body("data.content[0].tenantId", equalTo(tenantTwo.getId().toString()));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/ledger/summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalIncome", equalTo(3000.0f))
                .body("data.totalExpense", equalTo(1200.0f))
                .body("data.balance", equalTo(1800.0f));

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/ledger/summary")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.totalIncome", equalTo(9999.0f))
                .body("data.totalExpense", equalTo(0))
                .body("data.balance", equalTo(9999.0f));
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
