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
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the direct Visa API client interactions.
 * This test uses a mock Visa server to verify the client functionality.
 */
@Slf4j
@MicronautTest
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

    private HttpClient client;
    private MockVisaServer mockVisaServer;

    @Inject
    VisaClickToPayClient visaClient;

    @Inject
    VaultService vaultService;

    private final String correlationId = UUID.randomUUID().toString();
    private final String testConsumerId = "test-consumer-" + UUID.randomUUID();

    @Container
    static RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.11-management").withExposedPorts(5672, 15672);

    @BeforeAll
    static void beforeAll() {
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
        Mockito.reset(vaultService);
        when(vaultService.getApiKey()).thenReturn(Mono.just("test-api-key"));
        when(vaultService.getApiSecret()).thenReturn(Mono.just("test-api-secret"));
        if (client == null) {
            client = server.getApplicationContext().getBean(HttpClient.class);
        }
        if (mockVisaServer == null) {
            mockVisaServer = new MockVisaServer(client, server.getURL().toString());
        }
    }

    @Value("${visa.api.base-url}")
    String baseUrl;

    @Test
    void testEnrollDataRequest() {

        mockVisaServer.mockEnrollDataEndpoint(new RequestIdResponse("test-trace-id"));
        log.info("visa base url: {}", baseUrl);
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
                .paymentInstruments(Collections.singletonList(CardPaymentInstrument.builder()
                        .type("CARD")
                        .accountNumber("4111111111111111")
                        .nameOnCard("John Doe")
                        .expirationDate("2025-12")
                        .build()))
                .build();

        visaClient.enrollData(request, correlationId);

        StepVerifier.create(visaClient.enrollData(request, correlationId))
                .expectNextMatches(r -> "test-trace-id".equals(r.requestTraceId()))
                .verifyComplete();
    }

    @Test
    void testGetDataRequest() {
        MockVisaServer mockVisaServer =
                new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockGetDataEndpoint();

        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(testConsumerId)
                        .build())
                .build();

        StepVerifier.create(visaClient.getData(request, correlationId))
                .expectNextMatches(
                        response -> response.data() != null && !response.data().isEmpty())
                .verifyComplete();
    }

    @Test
    void testRequestStatusFetch() {

        MockVisaServer mockVisaServer =
                new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockRequestStatusEndpoint("COMPLETED");

        String requestTraceId = "test-trace-id-" + UUID.randomUUID();

        StepVerifier.create(visaClient.getRequestStatus(requestTraceId, correlationId))
                .expectNextMatches(response ->
                        response.status() != null && response.status().equals("COMPLETED"))
                .verifyComplete();
    }

    @Test
    void testAuthenticationHeadersAreAdded() {
        MockVisaServer mockVisaServer =
                new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.captureRequestHeaders();

        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(testConsumerId)
                        .build())
                .build();

        visaClient.getData(request, correlationId).block();

        HttpRequest<?> capturedRequest = mockVisaServer.getCapturedRequest();
        assert capturedRequest != null;

        MutableHttpRequest<?> mutableCapturedRequest = capturedRequest.toMutableRequest();
        log.info("Captured headers: {}", mutableCapturedRequest.getHeaders().asMap());

        assert mutableCapturedRequest.getHeaders().contains("Authorization");
        assert mutableCapturedRequest.getHeaders().contains("X-Request-Timestamp");
        assert mutableCapturedRequest.getHeaders().get("X-Correlation-Id").equals(correlationId);
    }

    @Test
    void testErrorHandling() {
        MockVisaServer mockVisaServer =
                new MockVisaServer(client, server.getURL().toString());
        mockVisaServer.mockErrorResponse(400, "InvalidParameter", "Invalid consumer ID format");

        GetDataRequest request = GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID("invalid-id")
                        .build())
                .build();

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

        public MockVisaServer(HttpClient client, String baseUrl) {
            this.client = client;
            this.baseUrl = baseUrl;
        }

        public void mockEnrollDataEndpoint(RequestIdResponse response) {
            String path = "/visaIdCredential/v1/enrollData";

            try {
                HttpResponse<Object> exchange = client.toBlocking()
                        .exchange(HttpRequest.POST(
                                baseUrl + "/mock-setup", Map.of("path", path, "status", 202, "response", response)));
                if (exchange != null) {
                    log.info("got response {}", exchange);
                }
            } catch (Exception e) {
                log.error("failed to add mock enroll data", e);
            }
        }

        public void mockGetDataEndpoint() {
            String path = "/visaIdCredential/v1/getData";

            GetDataResponse response = MockResponseUtil.createMockGetDataResponse("test-consumer");

            client.toBlocking()
                    .exchange(HttpRequest.POST(
                            baseUrl + "/mock-setup", Map.of("path", path, "status", 200, "response", response)));
        }

        public void mockRequestStatusEndpoint(String status) {
            String path = "/visaIdCredential/v1/requestStatus/.*";

            RequestStatusResponse response = MockResponseUtil.createMockRequestStatusResponse(status, "test-consumer");

            client.toBlocking()
                    .exchange(HttpRequest.POST(
                            baseUrl + "/mock-setup",
                            Map.of("path", path, "status", 200, "response", response, "isRegex", true)));
        }

        public void mockErrorResponse(int statusCode, String reason, String message) {
            String path = "/visaIdCredential/v1/getData";

            Map<String, Object> errorResponse =
                    Map.of("reason", reason, "message", message, "details", Collections.emptyList());

            client.toBlocking()
                    .exchange(HttpRequest.POST(
                            baseUrl + "/mock-setup",
                            Map.of("path", path, "status", statusCode, "response", errorResponse)));
        }

        public void captureRequestHeaders() {
            String path = "/visaIdCredential/v1/getData";

            client.toBlocking().exchange(HttpRequest.POST(baseUrl + "/mock-capture", Map.of("path", path)));

        }

        public HttpRequest<?> getCapturedRequest() {
            HttpResponse<Map> response =
                    client.toBlocking().exchange(HttpRequest.GET(baseUrl + "/mock-captured-request"), Map.class);

            if (response != null && response.body() != null) {
                Map<String, Object> requestData = response.body();
                MutableHttpRequest<?> mutableCapturedRequest = HttpRequest.GET("/").toMutableRequest();

                if (requestData.containsKey("headers")) {
                    Map<String, String> headers = (Map<String, String>) requestData.get("headers");
                    headers.forEach(mutableCapturedRequest::header);
                }

                return mutableCapturedRequest;
            }

            return null;
        }
    }
}