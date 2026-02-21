package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class StaffPayrollCompletionIT extends IntegrationTestBase {

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
    void staffLeaveSalaryStructureAndPayrollDetailFlowShouldWorkEndToEnd() {
        TenantEntity tenant = createTenant("M8 Staff Payroll Society");
        UnitEntity unit = createUnit(tenant.getId(), "SP-101");
        UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin", "admin.staffpayroll@shield.dev", UserRole.ADMIN);

        String token = login(admin.getEmail(), PASSWORD);

        String staffId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "employeeId", "STF-100",
                        "firstName", "Aarav",
                        "lastName", "Shah",
                        "phone", "9999990010",
                        "email", "aarav.staff@shield.dev",
                        "designation", "MANAGER",
                        "dateOfJoining", LocalDate.of(2026, 1, 1).toString(),
                        "employmentType", "FULL_TIME",
                        "basicSalary", 25000,
                        "active", true))
                .when()
                .post("/staff")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.id");

        String earningComponentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "componentName", "Basic",
                        "componentType", "EARNING",
                        "taxable", true))
                .when()
                .post("/payroll-components")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.componentName", equalTo("Basic"))
                .extract()
                .path("data.id");

        String deductionComponentId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "componentName", "PF",
                        "componentType", "DEDUCTION",
                        "taxable", false))
                .when()
                .post("/payroll-components")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.componentName", equalTo("PF"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "payrollComponentId", earningComponentId,
                        "amount", 30000,
                        "active", true,
                        "effectiveFrom", "2026-01-01"))
                .when()
                .post("/staff/{id}/salary-structure", staffId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "payrollComponentId", deductionComponentId,
                        "amount", 1000,
                        "active", true,
                        "effectiveFrom", "2026-01-01"))
                .when()
                .post("/staff/{id}/salary-structure", staffId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/staff/{id}/salary-structure", staffId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(2));

        for (String date : List.of("2026-02-10", "2026-02-11")) {
            given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of("staffId", staffId, "attendanceDate", date))
                    .when()
                    .post("/staff-attendance/check-in")
                    .then()
                    .statusCode(HttpStatus.OK.value());

            given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of("staffId", staffId, "attendanceDate", date))
                    .when()
                    .post("/staff-attendance/check-out")
                    .then()
                    .statusCode(HttpStatus.OK.value());
        }

        String payrollId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "staffId", staffId,
                        "month", 2,
                        "year", 2026,
                        "workingDays", 2,
                        "totalDeductions", 200))
                .when()
                .post("/payroll/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.grossSalary", equalTo(30000.0F))
                .body("data.totalDeductions", equalTo(1200.0F))
                .body("data.netSalary", equalTo(28800.0F))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/payroll/{id}/payslip", payrollId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.earnings.size()", equalTo(1))
                .body("data.deductions.size()", equalTo(1))
                .body("data.manualDeductions", equalTo(200.0F));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "payrollIds", List.of(payrollId),
                        "paymentMethod", "BANK_TRANSFER",
                        "paymentReferencePrefix", "M8-BATCH",
                        "paymentDate", "2026-02-28",
                        "payslipBaseUrl", "https://files.example/payslips"))
                .when()
                .post("/payroll/bulk-process")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.size()", equalTo(1))
                .body("data[0].status", equalTo("PROCESSED"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/payroll/{id}/approve", payrollId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PAID"));

        String leaveId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                        "staffId", staffId,
                        "leaveType", "CASUAL",
                        "fromDate", "2026-03-01",
                        "toDate", "2026-03-02",
                        "numberOfDays", 2,
                        "reason", "Personal"))
                .when()
                .post("/staff-leaves")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/staff-leaves/pending-approval")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/staff-leaves/{id}/approve", leaveId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("APPROVED"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/staff-leaves/balance/{staffId}", staffId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.approvedDays", equalTo(2));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/staff/export")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType("text/csv")
                .body(containsString("STF-100"));
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
