package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class BillingPaymentsModuleIT extends IntegrationTestBase {

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
    void billingLifecycleShouldHandleInvoiceReminderPaymentAndRefund() {
        TenantEntity tenant = createTenant("M3 Billing Society");
        UnitEntity unit = createUnit(tenant.getId(), "M3-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin M3", "admin.m3@shield.dev", UserRole.ADMIN);

        String token = login(admin.getEmail(), PASSWORD);

        String cycleId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "cycleName", "March 2026",
                        "month", 3,
                        "year", 2026,
                        "dueDate", LocalDate.of(2026, 3, 31).toString(),
                        "lateFeeApplicableDate", LocalDate.of(2026, 4, 5).toString()))
                .when()
                .post("/billing-cycles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("DRAFT"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/billing-cycles/{id}/publish", cycleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PUBLISHED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "billingCycleId", cycleId,
                        "baseAmount", 1000,
                        "calculationMethod", "EQUAL_SHARE",
                        "areaBasedAmount", 0,
                        "fixedAmount", 1000,
                        "totalAmount", 1000))
                .when()
                .post("/maintenance-charges/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.id", notNullValue());

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/maintenance-charges/cycle/{cycleId}", cycleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", hasSize(1));

        String invoiceId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "billingCycleId", cycleId,
                        "invoiceDate", LocalDate.of(2026, 3, 1).toString(),
                        "dueDate", LocalDate.of(2026, 3, 10).toString(),
                        "subtotal", 1000,
                        "lateFee", 100,
                        "gstAmount", 180,
                        "otherCharges", 20))
                .when()
                .post("/invoices/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("UNPAID"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "invoiceId", invoiceId,
                        "reminderType", "ON_DUE",
                        "channel", "EMAIL"))
                .when()
                .post("/payment-reminders/send")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SENT"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "invoiceId", invoiceId,
                        "reminderType", "AFTER_DUE",
                        "channel", "EMAIL",
                        "scheduledAt", Instant.parse("2026-03-11T10:00:00Z").toString()))
                .when()
                .post("/payment-reminders/schedule")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SCHEDULED"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payment-reminders/invoice/{invoiceId}", invoiceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", hasSize(2));

        String cashPaymentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "invoiceId", invoiceId,
                        "amount", 500,
                        "transactionRef", "CASH-M3-1"))
                .when()
                .post("/payments/cash")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.paymentStatus", equalTo("SUCCESS"))
                .extract()
                .path("data.id");

        String partialOutstanding = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/invoices/{id}", invoiceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PARTIALLY_PAID"))
                .extract()
                .path("data.outstandingAmount")
                .toString();
        assertEquals(new BigDecimal("800.00"), money(partialOutstanding));

        String chequePaymentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "invoiceId", invoiceId,
                        "amount", 800,
                        "chequeNumber", "001122",
                        "transactionRef", "CHQ-M3-1"))
                .when()
                .post("/payments/cheque")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.paymentStatus", equalTo("SUCCESS"))
                .extract()
                .path("data.id");

        String zeroOutstanding = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/invoices/{id}", invoiceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PAID"))
                .extract()
                .path("data.outstandingAmount")
                .toString();
        assertEquals(new BigDecimal("0.00"), money(zeroOutstanding));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payments/invoice/{invoiceId}", invoiceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", hasSize(2));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payments/{id}/receipt", cashPaymentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.receiptUrl", notNullValue());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of("reason", "Cheque bounced"))
                .when()
                .post("/payments/{id}/refund", chequePaymentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.paymentStatus", equalTo("REFUNDED"));

        String refundedOutstanding = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/invoices/{id}", invoiceId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PARTIALLY_PAID"))
                .extract()
                .path("data.outstandingAmount")
                .toString();
        assertEquals(new BigDecimal("800.00"), money(refundedOutstanding));

        String lateFeeRuleId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "ruleName", "Standard late fee",
                        "daysAfterDue", 5,
                        "feeType", "PERCENTAGE",
                        "feeAmount", 2.5))
                .when()
                .post("/late-fee-rules")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/late-fee-rules/{id}/deactivate", lateFeeRuleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(false));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/late-fee-rules/{id}/activate", lateFeeRuleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(true));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/billing-cycles/{id}/close", cycleId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("CLOSED"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payments/unit/{unitId}", unit.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data", hasSize(2));
    }

    @Test
    void billingResourcesShouldEnforceTenantIsolation() {
        TenantEntity tenantOne = createTenant("M3 Isolation One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "ISO-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.iso.one@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("M3 Isolation Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "ISO-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.iso.two@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String invoiceId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "dueDate", LocalDate.of(2026, 3, 20).toString(),
                        "subtotal", 900,
                        "lateFee", 0,
                        "gstAmount", 0,
                        "otherCharges", 0))
                .when()
                .post("/invoices/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/invoices/{id}", invoiceId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenTwo)
                .body(Map.of(
                        "invoiceId", invoiceId,
                        "amount", 100,
                        "transactionRef", "CASH-ISO-1"))
                .when()
                .post("/payments/cash")
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

    private BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
