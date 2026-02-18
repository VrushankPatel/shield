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

class PaymentGatewayFlowsIT extends IntegrationTestBase {

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
    void gatewayFlowShouldInitiateVerifyAndEnforceTenantIsolation() {
        TenantEntity tenantOne = createTenant("Payments Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "P-101");
        UserEntity adminOne = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.pay.one@shield.dev", UserRole.ADMIN);

        TenantEntity tenantTwo = createTenant("Payments Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "P-202");
        UserEntity adminTwo = createUser(tenantTwo.getId(), unitTwo.getId(), "Admin Two", "admin.pay.two@shield.dev", UserRole.ADMIN);

        String tokenOne = login(adminOne.getEmail(), PASSWORD);
        String tokenTwo = login(adminTwo.getEmail(), PASSWORD);

        String billId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "month", 2,
                        "year", 2026,
                        "amount", 2500,
                        "dueDate", LocalDate.of(2026, 2, 28).toString(),
                        "lateFee", 100))
                .when()
                .post("/billing/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String transactionRef = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "billId", billId,
                        "amount", 2500,
                        "mode", "UPI",
                        "provider", "razorpay"))
                .when()
                .post("/payments/initiate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.transactionRef", startsWith("PGTXN-"))
                .body("data.provider", equalTo("RAZORPAY"))
                .extract()
                .path("data.transactionRef");

        String paymentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "transactionRef", transactionRef,
                        "gatewayPaymentId", "pay_live_001",
                        "success", true,
                        "failureReason", ""))
                .when()
                .post("/payments/verify")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SUCCESS"))
                .body("data.paymentId", notNullValue())
                .extract()
                .path("data.paymentId");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + tokenOne)
                .body(Map.of(
                        "transactionRef", transactionRef,
                        "gatewayPaymentId", "pay_live_001",
                        "success", true,
                        "failureReason", ""))
                .when()
                .post("/payments/verify")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SUCCESS"))
                .body("data.paymentId", equalTo(paymentId));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/payments/{id}", paymentId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.transactionRef", equalTo(transactionRef));

        given()
                .header("Authorization", "Bearer " + tokenOne)
                .when()
                .get("/payments/transaction/{transactionRef}", transactionRef)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("SUCCESS"))
                .body("data.paymentId", equalTo(paymentId));

        given()
                .header("Authorization", "Bearer " + tokenTwo)
                .when()
                .get("/payments/{id}", paymentId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void callbackFlowShouldCaptureFailureStatus() {
        TenantEntity tenant = createTenant("Payments Callback Society");
        UnitEntity unit = createUnit(tenant.getId(), "P-303");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin Callback", "admin.pay.callback@shield.dev", UserRole.ADMIN);

        String token = login(admin.getEmail(), PASSWORD);

        String billId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "unitId", unit.getId(),
                        "month", 3,
                        "year", 2026,
                        "amount", 1800,
                        "dueDate", LocalDate.of(2026, 3, 25).toString(),
                        "lateFee", 0))
                .when()
                .post("/billing/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String transactionRef = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "billId", billId,
                        "amount", 1800,
                        "mode", "CARD",
                        "provider", "stripe"))
                .when()
                .post("/payments/initiate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.transactionRef");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "transactionRef", transactionRef,
                        "gatewayOrderId", "ord_cb_01",
                        "gatewayPaymentId", "pay_cb_01",
                        "status", "FAILED",
                        "payload", "{\"reason\":\"card_declined\"}",
                        "signature", "sig-sample"))
                .when()
                .post("/payments/callback")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("FAILED"))
                .body("data.paymentId", equalTo(null));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payments/transaction/{transactionRef}", transactionRef)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("FAILED"));
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
