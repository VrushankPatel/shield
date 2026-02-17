package com.shield.integration;

import static io.restassured.RestAssured.given;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class TenantIsolationIT extends IntegrationTestBase {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Test
    void shouldReturnOnlyAuthenticatedTenantUnits() {
        TenantEntity tenant1 = createTenant("Alpha Society");
        TenantEntity tenant2 = createTenant("Beta Society");

        createUnit(tenant1.getId(), "A-101");
        createUnit(tenant2.getId(), "B-202");

        createUser(tenant1.getId(), "admin.alpha@shield.dev", "password123");
        createUser(tenant2.getId(), "admin.beta@shield.dev", "password123");

        String accessToken = given()
                .contentType("application/json")
                .body(Map.of("email", "admin.alpha@shield.dev", "password", "password123"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.accessToken");

        given()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when()
                .get("/units")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", equalTo(1))
                .body("data.content[0].unitNumber", equalTo("A-101"));
    }

    @Test
    void shouldRejectUnauthenticatedUnitAccess() {
        given()
                .when()
                .get("/units")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private TenantEntity createTenant(String name) {
        TenantEntity entity = new TenantEntity();
        entity.setName(name);
        entity.setAddress("Address");
        return tenantRepository.save(entity);
    }

    private void createUnit(UUID tenantId, String unitNumber) {
        UnitEntity unit = new UnitEntity();
        unit.setTenantId(tenantId);
        unit.setUnitNumber(unitNumber);
        unit.setBlock("A");
        unit.setType("FLAT");
        unit.setSquareFeet(BigDecimal.valueOf(1000));
        unit.setStatus(UnitStatus.ACTIVE);
        unitRepository.save(unit);
    }

    private void createUser(UUID tenantId, String email, String password) {
        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setName("Admin");
        user.setEmail(email);
        user.setPhone("9999999999");
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
