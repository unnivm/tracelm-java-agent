package org.usbtechno.collector;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(ApiKeyProtectedTraceResourceProfile.class)
class ApiKeyProtectedTraceResourceTest {

    @Test
    void shouldRejectRequestsWithoutApiKey() {
        given()
                .when()
                .get("/traces")
                .then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test
    void shouldAllowRequestsWithApiKey() {
        given()
                .header("X-API-Key", "secret-key")
                .when()
                .get("/traces")
                .then()
                .statusCode(200);
    }
}
