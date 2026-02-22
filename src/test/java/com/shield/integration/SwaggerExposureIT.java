package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;

import com.shield.integration.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SwaggerExposureIT extends IntegrationTestBase {

    @Test
    void swaggerEndpointsShouldNotBePublicWhenDisabledByDefault() {
        given()
                .when()
                .get("/swagger-ui.html")
                .then()
                .statusCode(not(HttpStatus.OK.value()));

        given()
                .when()
                .get("/v3/api-docs")
                .then()
                .statusCode(not(HttpStatus.OK.value()));
    }
}
