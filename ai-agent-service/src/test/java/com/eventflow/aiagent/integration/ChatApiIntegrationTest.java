
package com.eventflow.aiagent.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnChatResponseWhenMessageIsValid() {

        String reply =
                given()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "message",
                                "Say hello in one short sentence."
                        ))
                        .when()
                        .post("/api/agent/chat")
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract()
                        .path("reply");

        assertNotNull(reply, "The assistant reply should not be null");
        assertFalse(reply.isBlank(), "The assistant reply should not be empty");
    }
    @Test
    void shouldReturnBadRequestWhenMessageIsBlank() {

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(Map.of("message", ""))
                .when()
                .post("/api/agent/chat")
                .then()
                .statusCode(400);
    }
    @Test
    void shouldReturnBadRequestWhenMessageExceedsMaxLength() {

        String longMessage = "a".repeat(2001);

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(Map.of("message", longMessage))
                .when()
                .post("/api/agent/chat")
                .then()
                .statusCode(400);
    }
}
