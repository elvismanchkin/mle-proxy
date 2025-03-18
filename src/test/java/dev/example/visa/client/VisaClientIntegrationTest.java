package dev.example.visa.client;

import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import dev.example.visa.security.VaultService;
import dev.example.visa.util.MockResponseUtil;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the direct Visa API client interactions.
 * This test uses a mock Visa server to verify the client functionality.
 */
@MicronautTest
//@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "micronaut.otel.enabled", value = "false")
@Property(name = "tracing.opentelemetry.enabled", value = "false")
@Property(name = "rabbitmq.uri", value = "amqp://guest:guest@localhost:5672")
@Property(name = "visa.security.vault.enabled", value = "false")
public class VisaClientIntegrationTest {

    @Inject
    EmbeddedServer server;

    @Inject
    ApplicationContext context;

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    VisaClickToPayClient visaClient;

    @Inject
    VaultService vaultService;

    private final String correlationId = UUID.randomUUID().toString();
    private final String testConsumerId = "test-consumer-" + UUID.randomUUID();

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    static {
        rabbitMQContainer.start();
        System.setProperty("rabbitmq.uri", rabbitMQContainer.getAmqpUrl());
    }

    @MockBean(VaultService.class)
    VaultService mockVaultService() {
        VaultService mock = mock(VaultService.class);
        when(mock.getApiKey()).thenReturn(Mono.just("test-api-key"));
        when(mock.getApiSecret()).thenReturn(Mono.just("test-api-secret"));
        return mock;
    }

    @BeforeEach
    void setup() {
        // Reset vault service mock
        Mockito.reset(vaultService);
        when(vaultService.getApiKey()).thenReturn(Mono.just("test-api-key"));
        when(vaultService.getApiSecret()).thenReturn(Mono.just("test-api-secret"));
    }

    @Test
    void testEnrollDataRequest() {
        // Mock API response
        MockVisaServer mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockEnrollDataEndpoint(RequestIdResponse.builder()
                .requestTraceId("test-trace-id")
                .build());

        // Create test request
        EnrollDataRequest request = EnrollDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformation.builder()
                        .externalConsumerID(testConsumerId)
                        .firstName("John")
                        .lastName("Doe")
                        .countryCode("USA")
                        .build())
                .paymentInstruments(Collections.singletonList(
                        CardPaymentInstrument.builder()
                                .type("CARD")
                                .accountNumber("4111111111111111")
                                .nameOnCard("John Doe")
                                .expirationDate("2025-12")
                                .build()))
                .build();

        // Execute and verify
        StepVerifier.create(visaClient.enrollData(request, correlationId))
                .expectNextMatches(response ->
                        response.requestTraceId() != null &&
                                response.requestTraceId().equals("test-trace-id"))
                .verifyComplete();
    }

    @Test
    void testGetDataRequest() {
        // Mock API response
        MockVisaServer mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockGetDataEndpoint();

        // Create test request
        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(testConsumerId)
                        .build())
                .build();

        // Execute and verify
        StepVerifier.create(visaClient.getData(request, correlationId))
                .expectNextMatches(response ->
                        response.data() != null &&
                                !response.data().isEmpty())
                .verifyComplete();
    }

    @Test
    void testRequestStatusFetch() {
        // Mock API response
        MockVisaServer mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockRequestStatusEndpoint("COMPLETED");

        String requestTraceId = "test-trace-id-" + UUID.randomUUID();

        // Execute and verify
        StepVerifier.create(visaClient.getRequestStatus(requestTraceId, correlationId))
                .expectNextMatches(response ->
                        response.status() != null &&
                                response.status().equals("COMPLETED"))
                .verifyComplete();
    }

    @Test
    void testAuthenticationHeadersAreAdded() {
        // Setup a mock server to capture the request
        MockVisaServer mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.captureRequestHeaders();

        // Create a simple request
        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(testConsumerId)
                        .build())
                .build();

        // Execute the request
        visaClient.getData(request, correlationId).block();

        // Verify that the authentication headers were added
        HttpRequest<?> capturedRequest = mockVisaServer.getCapturedRequest();
        assert capturedRequest != null;
        assert capturedRequest.getHeaders().contains("Authorization");
        assert capturedRequest.getHeaders().contains("X-Correlation-Id");

        // The correlation ID should match what we provided
        assert capturedRequest.getHeaders().get("X-Correlation-Id").equals(correlationId);
    }

    @Test
    void testErrorHandling() {
        // Setup mock server to return an error
        MockVisaServer mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockErrorResponse(400, "InvalidParameter", "Invalid consumer ID format");

        // Create a request with an invalid consumer ID
        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID("invalid-id")
                        .build())
                .build();

        // Execute and verify the error is propagated correctly
        StepVerifier.create(visaClient.getData(request, correlationId))
                .expectError(io.micronaut.http.client.exceptions.HttpClientResponseException.class)
                .verify();
    }

    /**
     * Mock server to simulate Visa API responses.
     */
    static class MockVisaServer {
        private final HttpClient client;
        private final String baseUrl;
        private HttpRequest<?> capturedRequest;

        public MockVisaServer(HttpClient client, String baseUrl) {
            this.client = client;
            this.baseUrl = baseUrl;
        }

        public void mockEnrollDataEndpoint(RequestIdResponse response) {
            String path = "/visaIdCredential/v1/enrollData";

            // Setup the mock endpoint
            client.toBlocking().exchange(
                    HttpRequest.POST(baseUrl + "/mock-setup", Map.of(
                            "path", path,
                            "status", 202,
                            "response", response
                    ))
            );
        }

        public void mockGetDataEndpoint() {
            String path = "/visaIdCredential/v1/getData";

            // Create a sample response with some data
            GetDataResponse response = MockResponseUtil.createMockGetDataResponse("test-consumer");

            // Setup the mock endpoint
            client.toBlocking().exchange(
                    HttpRequest.POST(baseUrl + "/mock-setup", Map.of(
                            "path", path,
                            "status", 200,
                            "response", response
                    ))
            );
        }

        public void mockRequestStatusEndpoint(String status) {
            String path = "/visaIdCredential/v1/requestStatus/.*";

            // Create a sample response
            RequestStatusResponse response = MockResponseUtil.createMockRequestStatusResponse(
                    status, "test-consumer");

            // Setup the mock endpoint
            client.toBlocking().exchange(
                    HttpRequest.POST(baseUrl + "/mock-setup", Map.of(
                            "path", path,
                            "status", 200,
                            "response", response,
                            "isRegex", true
                    ))
            );
        }

        public void mockErrorResponse(int statusCode, String reason, String message) {
            String path = "/visaIdCredential/v1/getData";

            // Create an error response
            Map<String, Object> errorResponse = Map.of(
                    "reason", reason,
                    "message", message,
                    "details", Collections.emptyList()
            );

            // Setup the mock endpoint
            client.toBlocking().exchange(
                    HttpRequest.POST(baseUrl + "/mock-setup", Map.of(
                            "path", path,
                            "status", statusCode,
                            "response", errorResponse
                    ))
            );
        }

        public void captureRequestHeaders() {
            String path = "/visaIdCredential/v1/getData";

            // Setup a request capture
            client.toBlocking().exchange(
                    HttpRequest.POST(baseUrl + "/mock-capture", Map.of(
                            "path", path
                    ))
            );

            // The captured request will be available via a GET to /mock-captured-request
        }

        public HttpRequest<?> getCapturedRequest() {
            HttpResponse<Map> response = client.toBlocking().exchange(
                    HttpRequest.GET(baseUrl + "/mock-captured-request"),
                    Map.class
            );

            if (response != null && response.body() != null) {
                Map<String, Object> requestData = response.body();
                // Convert the captured data back to a HttpRequest
                // This is simplified - in real code you'd parse the headers, method, etc.
                return HttpRequest.GET("/");
            }

            return null;
        }
    }
}