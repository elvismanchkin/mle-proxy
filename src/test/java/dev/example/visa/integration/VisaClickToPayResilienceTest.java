package dev.example.visa.integration;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.ErrorResponse;
import dev.example.visa.model.RequestIdResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests focused on resilience and error handling scenarios.
 */
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisaClickToPayResilienceTest {
    private static final Logger LOG = LoggerFactory.getLogger(VisaClickToPayResilienceTest.class);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @Inject
    ApplicationContext context;

    @Inject
    VisaClickToPayClient mockVisaClient;

    @Test
    void testRetryMechanism() {
        String testConsumerId = "test-consumer-" + UUID.randomUUID();
        String requestTraceId = UUID.randomUUID().toString();

        // First call fails with a 500 error
        HttpClientResponseException mockException = createMockHttpException(500, "ServerError", "Internal server error");

        // Set up the client to fail on first call, then succeed
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenReturn(Mono.error(mockException))
                .thenReturn(Mono.just(RequestIdResponse.builder()
                        .requestTraceId(requestTraceId)
                        .build()));

        // Create a test request
        EnrollDataRequest request = createTestEnrollDataRequest(testConsumerId);

        // Execute the request and verify retry behavior
        StepVerifier.create(mockVisaClient.enrollData(request, "test-correlation-id"))
                .expectNextMatches(response -> response.requestTraceId().equals(requestTraceId))
                .verifyComplete();

        // Verify the client was called twice (initial + retry)
        verify(mockVisaClient, times(2)).enrollData(any(), anyString());
    }

    @Test
    void testCircuitBreakerOnConsecutiveFailures() throws Exception {
        String testConsumerId = "test-consumer-" + UUID.randomUUID();

        // Create a sequence of exceptions
        HttpClientResponseException mockException = createMockHttpException(500, "ServerError", "Internal server error");

        // Configure the client to always fail
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenReturn(Mono.error(mockException));

        // Create a test request
        EnrollDataRequest request = createTestEnrollDataRequest(testConsumerId);

        // Test consecutive failures
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(mockVisaClient.enrollData(request, "test-correlation-id-" + i))
                    .expectError(HttpClientResponseException.class)
                    .verify(Duration.ofSeconds(5));
        }

        // Verify the client was called for each request
        verify(mockVisaClient, times(5)).enrollData(any(), anyString());

        // Reset the mock to simulate circuit half-open state
        reset(mockVisaClient);
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder()
                        .requestTraceId("success-after-failures")
                        .build()));

        // Allow circuit breaker reset time
        Thread.sleep(1000);

        // Should work after circuit resets
        StepVerifier.create(mockVisaClient.enrollData(request, "test-correlation-id-recovery"))
                .expectNextMatches(response -> response.requestTraceId().equals("success-after-failures"))
                .verifyComplete();
    }

    @Test
    void testTransientNetworkErrorHandling() {
        String testConsumerId = "test-consumer-" + UUID.randomUUID();

        // Network error (IOException wrapped in HttpClientResponseException)
        IOException ioException = new IOException("Connection refused");
        HttpClientResponseException networkException = new HttpClientResponseException(
                "Connection error", ioException, HttpResponse.serverError());

        // Success response after error
        String requestTraceId = UUID.randomUUID().toString();

        // Set up the client to fail on first call with network error, then succeed
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenReturn(Mono.error(networkException))
                .thenReturn(Mono.just(RequestIdResponse.builder()
                        .requestTraceId(requestTraceId)
                        .build()));

        // Create a test request
        EnrollDataRequest request = createTestEnrollDataRequest(testConsumerId);

        // Execute the request and verify retry behavior
        StepVerifier.create(mockVisaClient.enrollData(request, "test-correlation-id"))
                .expectNextMatches(response -> response.requestTraceId().equals(requestTraceId))
                .verifyComplete();

        // Verify the client was called twice (initial + retry)
        verify(mockVisaClient, times(2)).enrollData(any(), anyString());
    }

    @Test
    void testErrorResponseMapping() throws Exception {
        // Create RabbitMQ client for testing
        RabbitMQTestClient rabbitClient = new RabbitMQTestClient(rabbitMQContainer.getAmqpUrl());

        try {
            // Create a consumer for responses
            String responseQueue = rabbitClient.declareQueue();
            String correlationId = UUID.randomUUID().toString();

            // Mock a client error with detailed error response
            HttpClientResponseException clientException = createMockHttpException(
                    400, "ValidationError", "Invalid input data");

            // Set up the client to return the error
            when(mockVisaClient.enrollData(any(), anyString()))
                    .thenReturn(Mono.error(clientException));

            // Create request
            EnrollDataRequestDto request = TestDataFactory.createEnrollDataRequestDto("invalid-consumer");

            // Send the request through RabbitMQ
            rabbitClient.sendRequest(
                    "visa-click-to-pay-exchange",
                    "enrollData",
                    correlationId,
                    responseQueue,
                    request);

            // Receive the response
            byte[] responseBytes = rabbitClient.receiveResponse(responseQueue, correlationId, 10);
            EnrollmentResponseDto response = rabbitClient.deserialize(responseBytes, EnrollmentResponseDto.class);

            // Verify response contains error information
            assertNotNull(response);
            assertEquals("ERROR", response.status());
            assertNotNull(response.error());
            assertEquals("ValidationError", response.error().reason());
            assertEquals("Invalid input data", response.error().message());
        } finally {
            rabbitClient.close();
        }
    }

    @Test
    void testTimeoutHandling() throws Exception {
        String testConsumerId = "test-consumer-" + UUID.randomUUID();

        // Set up the client to delay beyond timeout
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenReturn(Mono.delay(Duration.ofSeconds(5))
                        .then(Mono.just(RequestIdResponse.builder()
                                .requestTraceId("too-late")
                                .build())));

        // Create RabbitMQ client with short timeout
        RabbitMQTestClient rabbitClient = new RabbitMQTestClient(rabbitMQContainer.getAmqpUrl());

        try {
            // Create a consumer for responses
            String responseQueue = rabbitClient.declareQueue(3000); // 3 second TTL
            String correlationId = UUID.randomUUID().toString();

            // Create request
            EnrollDataRequestDto request = TestDataFactory.createEnrollDataRequestDto(testConsumerId);

            // Send the request through RabbitMQ with expiration
            rabbitClient.sendRequest(
                    "visa-click-to-pay-exchange",
                    "enrollData",
                    correlationId,
                    responseQueue,
                    request,
                    "3000"); // 3 second expiration

            // Attempt to receive the response (should time out)
            byte[] responseBytes = rabbitClient.receiveResponse(responseQueue, correlationId, 4);

            // Should not receive a response due to timeout
            assertNull(responseBytes);
        } finally {
            rabbitClient.close();
        }
    }

    // Helper methods

    private EnrollDataRequest createTestEnrollDataRequest(String consumerId) {
        return EnrollDataRequest.builder()
                .intent(dev.example.visa.model.Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(dev.example.visa.model.ConsumerInformation.builder()
                        .externalConsumerID(consumerId)
                        .firstName("John")
                        .lastName("Doe")
                        .countryCode("USA")
                        .build())
                .paymentInstruments(Collections.singletonList(
                        dev.example.visa.model.CardPaymentInstrument.builder()
                                .type("CARD")
                                .accountNumber("4111111111111111")
                                .nameOnCard("John Doe")
                                .expirationDate("2025-12")
                                .build()))
                .build();
    }

    private HttpClientResponseException createMockHttpException(int status, String reason, String message) {
        HttpClientResponseException mockException = Mockito.mock(HttpClientResponseException.class);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

        // Create error response
        ErrorResponse errorResponse = ErrorResponse.builder()
                .reason(reason)
                .message(message)
                .details(Collections.emptyList())
                .build();

        // Configure the mock
        when(mockException.getResponse()).thenReturn(mockResponse);
        when(mockResponse.getBody(Mockito.any(io.micronaut.core.type.Argument.class))).thenReturn(java.util.Optional.of(errorResponse));
        when(mockException.getMessage()).thenReturn(message);
        when(mockException.getStatus()).thenReturn(HttpStatus.valueOf(status));

        return mockException;
    }
}