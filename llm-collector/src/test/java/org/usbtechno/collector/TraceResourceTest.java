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
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.usbtechno.collector.repository.TraceRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class TraceResourceTest {

    @Inject
    TraceRepository traceRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        traceRepository.deleteAll();
    }

    @Test
    void shouldCreateTraceAndReturnMetrics() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "traceId": "trace-1",
                          "model": "gpt-4o-mini",
                          "prompt": "hello",
                          "response": "world",
                          "latency": 120,
                          "timestamp": 1712800000000,
                          "status": "success",
                          "promptTokens": 15,
                          "responseTokens": 25,
                          "totalTokens": 40,
                          "tokenLength": 40,
                          "cost": 0.0123
                        }
                        """)
                .when()
                .post("/traces")
                .then()
                .statusCode(201)
                .body("traceId", equalTo("trace-1"))
                .body("qualityScore", notNullValue());

        given()
                .when()
                .get("/traces/metrics")
                .then()
                .statusCode(200)
                .body("totalRequests", equalTo(1))
                .body("avgLatency", notNullValue())
                .body("avgQualityScore", notNullValue())
                .body("totalTokens", equalTo(40))
                .body("totalCost", notNullValue());
    }

    @Test
    void shouldRejectInvalidTracePayload() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "traceId": "",
                          "model": "",
                          "latency": -5,
                          "status": "",
                          "cost": -1
                        }
                        """)
                .when()
                .post("/traces")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation failed"));
    }

    @Test
    void shouldSupportPagedFiltering() {
        createTrace("trace-a", "gpt-4o-mini", "success", 100, 1712800000000L);
        createTrace("trace-b", "gpt-4o-mini", "error", 220, 1712800001000L);
        createTrace("trace-c", "gpt-4.1", "success", 300, 1712800002000L);

        given()
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("model", "gpt-4o-mini")
                .when()
                .get("/traces/page")
                .then()
                .statusCode(200)
                .body("items", hasSize(2))
                .body("total", equalTo(2))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldReturnCostliestAndSlowestRequests() {
        createTrace("trace-1", "gpt-4o-mini", "success", 90, 1712800000000L, "cheap", 0.0010);
        createTrace("trace-2", "gpt-4.1", "success", 900, 1712800001000L, "slow", 0.0110);
        createTrace("trace-3", "gpt-4.1-mini", "success", 300, 1712800002000L, "expensive", 0.0210);

        given()
                .queryParam("limit", 2)
                .when()
                .get("/traces/costly-prompts")
                .then()
                .statusCode(200)
                .body("[0].traceId", equalTo("trace-3"))
                .body("[1].traceId", equalTo("trace-2"));

        given()
                .queryParam("limit", 2)
                .when()
                .get("/traces/slow-requests")
                .then()
                .statusCode(200)
                .body("[0].traceId", equalTo("trace-2"))
                .body("[1].traceId", equalTo("trace-3"));
    }

    private void createTrace(String traceId, String model, String status, long latency, long timestamp) {
        createTrace(traceId, model, status, latency, timestamp, "hello", 0.0100);
    }

    private void createTrace(String traceId, String model, String status, long latency, long timestamp, String prompt, double cost) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "traceId": "%s",
                          "model": "%s",
                          "prompt": "%s",
                          "response": "world",
                          "latency": %d,
                          "timestamp": %d,
                          "status": "%s",
                          "promptTokens": 10,
                          "responseTokens": 20,
                          "totalTokens": 30,
                          "tokenLength": 30,
                          "cost": %s
                        }
                        """.formatted(traceId, model, prompt, latency, timestamp, status, cost))
                .when()
                .post("/traces")
                .then()
                .statusCode(201);
    }
}
