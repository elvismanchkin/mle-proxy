package dev.example.visa.integration;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import dev.example.visa.util.MockResponseUtil;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for concurrent load and performance characteristics.
 */
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisaClickToPayConcurrentLoadTest {
    private static final Logger LOG = LoggerFactory.getLogger(VisaClickToPayConcurrentLoadTest.class);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @Inject
    ApplicationContext context;

    @Inject
    VisaClickToPayClient mockVisaClient;

    /**
     * Test handling of many concurrent requests via RabbitMQ.
     */
    @Test
    void testHighConcurrentLoad() throws Exception {
        // Number of concurrent requests
        final int REQUEST_COUNT = 50;

        // Configure mock client for all requests
        setupMockClientForConcurrentLoad();

        // Create RabbitMQ client for testing
        RabbitMQTestClient rabbitClient = new RabbitMQTestClient(rabbitMQContainer.getAmqpUrl());

        try {
            // Create a shared response queue
            String responseQueue = rabbitClient.declareQueue();

            // Track successful responses
            CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);
            AtomicInteger successCount = new AtomicInteger(0);

            // Executor for handling responses
            ExecutorService executor = Executors.newSingleThreadExecutor();

            // Start a consumer for responses
            CompletableFuture<Void> consumerFuture = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < REQUEST_COUNT; i++) {
                    try {
                        // Each response has its own correlation ID
                        String correlationId = "concurrent-" + i;

                        // Wait for response
                        byte[] responseBytes = rabbitClient.receiveResponse(responseQueue, correlationId, 15);

                        if (responseBytes != null) {
                            EnrollmentResponseDto response = rabbitClient.deserialize(
                                    responseBytes, EnrollmentResponseDto.class);

                            if (response != null && "SUCCESS".equals(response.status())) {
                                successCount.incrementAndGet();
                            }
                        }

                        latch.countDown();
                    } catch (Exception e) {
                        LOG.error("Error processing response", e);
                        latch.countDown();
                    }
                }
            }, executor);

            // Send many concurrent requests
            for (int i = 0; i < REQUEST_COUNT; i++) {
                String correlationId = "concurrent-" + i;
                String consumerId = "test-concurrent-" + i;

                // Send the request
                rabbitClient.sendRequest(
                        "visa-click-to-pay-exchange",
                        "enrollData",
                        correlationId,
                        responseQueue,
                        TestDataFactory.createEnrollDataRequestDto(consumerId));
            }

            // Wait for all responses to be processed
            boolean completed = latch.await(30, TimeUnit.SECONDS);

            // Shut down executor
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Verify results
            assertTrue(completed, "Failed to process all responses in time");
            assertEquals(REQUEST_COUNT, successCount.get(), "Not all requests succeeded");

        } finally {
            rabbitClient.close();
        }
    }

    /**
     * Test multiple concurrent request types (mixed workload).
     */
    @Test
    void testMixedWorkload() throws Exception {
        // Configure mock client for all request types
        setupMockClientForMixedWorkload();

        // List of request types to send
        List<String> requestTypes = List.of(
                "enrollData",
                "enrollPaymentInstruments",
                "requestStatus",
                "getData",
                "managePaymentInstruments",
                "manageConsumerInformation",
                "deleteConsumerInformation",
                "deletePaymentInstruments"
        );

        // Number of requests per type
        final int REQUESTS_PER_TYPE = 5;
        final int TOTAL_REQUESTS = requestTypes.size() * REQUESTS_PER_TYPE;

        // Create RabbitMQ client for testing
        RabbitMQTestClient rabbitClient = new RabbitMQTestClient(rabbitMQContainer.getAmqpUrl());

        try {
            // Create a shared response queue
            String responseQueue = rabbitClient.declareQueue();

            // Track successful responses
            CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);
            AtomicInteger successCount = new AtomicInteger(0);

            // Executor for handling responses
            ExecutorService executor = Executors.newFixedThreadPool(3);

            // Start a consumer for responses
            CompletableFuture<Void> consumerFuture = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < TOTAL_REQUESTS; i++) {
                    try {
                        // Each response has its own correlation ID
                        String correlationId = "mixed-" + i;

                        // Wait for response
                        byte[] responseBytes = rabbitClient.receiveResponse(responseQueue, correlationId, 15);

                        if (responseBytes != null) {
                            // We don't know the exact response type, but we can check for success status
                            String responseJson = new String(responseBytes);
                            if (responseJson.contains("\"status\":\"SUCCESS\"")) {
                                successCount.incrementAndGet();
                            }
                        }

                        latch.countDown();
                    } catch (Exception e) {
                        LOG.error("Error processing response", e);
                        latch.countDown();
                    }
                }
            }, executor);

            // Send mixed concurrent requests
            int requestIndex = 0;
            for (String requestType : requestTypes) {
                for (int i = 0; i < REQUESTS_PER_TYPE; i++) {
                    String correlationId = "mixed-" + requestIndex++;
                    String consumerId = "test-mixed-" + UUID.randomUUID();

                    // Create appropriate request object based on type
                    Object requestObj = createRequestForType(requestType, consumerId);

                    // Send the request
                    rabbitClient.sendRequest(
                            "visa-click-to-pay-exchange",
                            requestType,
                            correlationId,
                            responseQueue,
                            requestObj);
                }
            }

            // Wait for all responses to be processed
            boolean completed = latch.await(30, TimeUnit.SECONDS);

            // Shut down executor
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Verify results
            assertTrue(completed, "Failed to process all responses in time");
            LOG.info("Successfully processed {} out of {} requests", successCount.get(), TOTAL_REQUESTS);
            assertTrue(successCount.get() >= TOTAL_REQUESTS * 0.9,
                    "Less than 90% of requests succeeded");

        } finally {
            rabbitClient.close();
        }
    }

    /**
     * Test reactive request batching.
     */
    @Test
    void testReactiveBatchProcessing() {
        // Number of consumers to process
        final int CONSUMER_COUNT = 20;

        // Set up mock responses
        setupMockClientForBatchProcessing();

        // Generate list of consumer IDs
        List<String> consumerIds = new ArrayList<>(CONSUMER_COUNT);
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumerIds.add("test-batch-" + i);
        }

        // Create a list of data requests
        List<GetDataRequest> requests = consumerIds.stream()
                .map(this::createGetDataRequest)
                .toList();

        // Process all requests in parallel using Flux
        long startTime = System.currentTimeMillis();

        List<GetDataResponse> results = Flux.fromIterable(requests)
                .flatMap(request ->
                        mockVisaClient.getData(request, UUID.randomUUID().toString()))
                .collectList()
                .block(Duration.ofSeconds(30));

        long duration = System.currentTimeMillis() - startTime;

        // Verify results
        Assertions.assertNotNull(results);
        assertEquals(CONSUMER_COUNT, results.size());

        LOG.info("Processed {} requests in {} ms (avg {} ms/request)",
                CONSUMER_COUNT, duration, duration / CONSUMER_COUNT);
    }

    // Helper methods

    private void setupMockClientForConcurrentLoad() {
        // Set up mock to always return success for any request
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));
    }

    private void setupMockClientForMixedWorkload() {
        // Enroll data
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));

        // Enroll payment instruments
        when(mockVisaClient.enrollPaymentInstruments(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));

        // Get request status
        when(mockVisaClient.getRequestStatus(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String requestTraceId = invocation.getArgument(0);
                    String consumerId = extractConsumerIdFromTraceId(requestTraceId);
                    return Mono.just(MockResponseUtil.createMockRequestStatusResponse(
                            "COMPLETED", consumerId));
                });

        // Get data
        when(mockVisaClient.getData(any(), anyString()))
                .thenAnswer(invocation -> {
                    GetDataRequest request = invocation.getArgument(0);
                    String consumerId = request.consumerInformation().externalConsumerID();
                    return Mono.just(MockResponseUtil.createMockGetDataResponse(consumerId));
                });

        // Manage payment instruments
        when(mockVisaClient.managePaymentInstruments(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));

        // Manage consumer information
        when(mockVisaClient.manageConsumerInformation(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));

        // Delete consumer information
        when(mockVisaClient.deleteConsumerInformation(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));

        // Delete payment instruments
        when(mockVisaClient.deletePaymentInstruments(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(RequestIdResponse.builder()
                        .requestTraceId(UUID.randomUUID().toString())
                        .build()));
    }

    private void setupMockClientForBatchProcessing() {
        // Get data - simulate some processing time for each request
        when(mockVisaClient.getData(any(), anyString()))
                .thenAnswer(invocation -> {
                    GetDataRequest request = invocation.getArgument(0);
                    String consumerId = request.consumerInformation().externalConsumerID();

                    // Simulate some processing delay
                    return Mono.delay(Duration.ofMillis(50))
                            .then(Mono.just(MockResponseUtil.createMockGetDataResponse(consumerId)));
                });
    }

    private GetDataRequest createGetDataRequest(String consumerId) {
        return GetDataRequest.builder()
                .intent(dev.example.visa.model.Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(dev.example.visa.model.ConsumerInformationIdRef.builder()
                        .externalConsumerID(consumerId)
                        .build())
                .build();
    }

    private Object createRequestForType(String requestType, String consumerId) {
        return switch (requestType) {
            case "enrollData" -> TestDataFactory.createEnrollDataRequestDto(consumerId);
            case "enrollPaymentInstruments" -> TestDataFactory.createEnrollPaymentInstrumentsRequestDto(consumerId);
            case "requestStatus" -> UUID.randomUUID().toString(); // Just the trace ID
            case "getData" -> TestDataFactory.createGetDataRequestDto(consumerId);
            case "managePaymentInstruments" -> TestDataFactory.createManagePaymentInstrumentsRequestDto(consumerId);
            case "manageConsumerInformation" -> TestDataFactory.createManageConsumerInformationRequestDto(consumerId);
            case "deleteConsumerInformation" -> TestDataFactory.createDeleteConsumerInformationRequestDto(consumerId);
            case "deletePaymentInstruments" -> TestDataFactory.createDeletePaymentInstrumentsRequestDto(consumerId);
            default -> throw new IllegalArgumentException("Unknown request type: " + requestType);
        };
    }

    private String extractConsumerIdFromTraceId(String requestTraceId) {
        // In real implementation, you might have a mapping between trace IDs and consumer IDs
        // For testing, we just return a fixed consumer ID
        return "test-consumer";
    }
}