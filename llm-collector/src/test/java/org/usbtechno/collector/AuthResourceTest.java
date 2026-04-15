package org.usbtechno.collector;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.usbtechno.collector.auth.AuthService;
import org.usbtechno.collector.repository.UserAccountRepository;
import org.usbtechno.collector.repository.UserSessionRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class AuthResourceTest {

    @Inject
    UserSessionRepository userSessionRepository;

    @Inject
    UserAccountRepository userAccountRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        userSessionRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void shouldSignupLoginAndLogout() {
        ExtractableResponse<Response> signup = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Test User",
                          "email": "user@example.com",
                          "password": "strongpass123"
                        }
                        """)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201)
                .body("email", equalTo("user@example.com"))
                .extract();

        String sessionCookie = signup.cookie(AuthService.SESSION_COOKIE);

        given()
                .cookie(AuthService.SESSION_COOKIE, sessionCookie)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test User"));

        given()
                .cookie(AuthService.SESSION_COOKIE, sessionCookie)
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(204);

        given()
                .cookie(AuthService.SESSION_COOKIE, sessionCookie)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldLoginExistingUser() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Demo User",
                          "email": "demo@example.com",
                          "password": "strongpass123"
                        }
                        """)
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "demo@example.com",
                          "password": "strongpass123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("email", equalTo("demo@example.com"));
    }
}
