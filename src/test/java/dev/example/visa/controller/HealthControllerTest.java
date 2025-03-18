package dev.example.visa.controller;

import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import jakarta.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class HealthControllerTest {

    @Inject
    HealthClient healthClient;

    @Test
    void testHealthEndpoint() {
        StepVerifier.create(healthClient.health())
                .assertNext(response -> {
                    // Verify response status
                    assertEquals(200, response.code());

                    // Get the response body
                    Map<String, Object> responseBody = response.body();
                    assertNotNull(responseBody);

                    // Verify required fields exist
                    assertTrue(responseBody.containsKey("status"));
                    assertTrue(responseBody.containsKey("timestamp"));
                    assertTrue(responseBody.containsKey("version"));
                    assertTrue(responseBody.containsKey("serviceName"));

                    // Verify status field
                    assertEquals(HealthStatus.UP.toString(), responseBody.get("status"));

                    // Verify service name
                    assertEquals("visa-click-to-pay-proxy", responseBody.get("serviceName"));

                    // Verify timestamp and version are not empty
                    assertNotNull(responseBody.get("timestamp"));
                    assertNotNull(responseBody.get("version"));
                })
                .verifyComplete();
    }

    @Client("/")
    interface HealthClient {
        @io.micronaut.http.annotation.Get("/health")
        Mono<HttpResponse<Map<String, Object>>> health();
    }
}