/*
 * Copyright 2026 USBTechnologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
