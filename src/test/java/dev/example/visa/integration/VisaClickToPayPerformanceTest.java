package dev.example.visa.integration;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.service.VisaClickToPayCoordinator;
import dev.example.visa.util.MockResponseUtil;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests focused on measuring performance characteristics.
 */
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisaClickToPayPerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(VisaClickToPayPerformanceTest.class);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @Inject
    ApplicationContext context;

    @Inject
    VisaClickToPayClient mockVisaClient;

    @Inject
    VisaClickToPayCoordinator coordinator;

    private RabbitMQTestClient rabbitClient;

    @BeforeAll
    void setup() throws Exception {
        rabbitClient = new RabbitMQTestClient(rabbitMQContainer.getAmqpUrl());

        // Set up common mocks
        setupMockResponses();
    }

    @AfterAll
    void tearDown() throws Exception {
        if (rabbitClient != null) {
            rabbitClient.close();
        }
    }

    @Test
    void testDirectVsRabbitMQPerformance() throws Exception {
        // Number of requests to send
        final int REQUEST_COUNT = 100;

        String testConsumerId = "perf-test-" + UUID.randomUUID();

        // Measure direct API performance
        EnrollDataRequestDto request = TestDataFactory.createEnrollDataRequestDto(testConsumerId);

        // Warm-up
        coordinator.enrollDataMapped(
                coordinator.getVisaMapper().mapToEnrollDataRequest(request)).block();

        // Measure direct API latency
        long directStart = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            coordinator.enrollDataMapped(
                    coordinator.getVisaMapper().mapToEnrollDataRequest(request)).block();
        }

        long directDuration = System.currentTimeMillis() - directStart;
        double directAvg = (double) directDuration / REQUEST_COUNT;

        LOG.info("Direct API: {} requests in {}ms (avg: {}ms/request)",
                REQUEST_COUNT, directDuration, directAvg);

        // Measure RabbitMQ performance
        String responseQueue = rabbitClient.declareQueue();
        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);
        AtomicLong totalRabbitTime = new AtomicLong(0);

        // Start receiving responses
        Thread responseThread = new Thread(() -> {
            for (int i = 0; i < REQUEST_COUNT; i++) {
                try {
                    String correlationId = "perf-" + i;
                    long start = System.currentTimeMillis();

                    byte[] responseBytes = rabbitClient.receiveResponse(responseQueue, correlationId, 30);

                    if (responseBytes != null) {
                        EnrollmentResponseDto response = rabbitClient.deserialize(
                                responseBytes, EnrollmentResponseDto.class);

                        if (response != null) {
                            long end = System.currentTimeMillis();
                            totalRabbitTime.addAndGet(end - start);
                        }
                    }

                    latch.countDown();
                } catch (Exception e) {
                    LOG.error("Error processing response", e);
                    latch.countDown();
                }
            }
        });

        responseThread.start();

        // Send requests via RabbitMQ
        long rabbitStart = System.currentTimeMillis();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            String correlationId = "perf-" + i;

            rabbitClient.sendRequest(
                    "visa-click-to-pay-exchange",
                    "enrollData",
                    correlationId,
                    responseQueue,
                    request);
        }

        // Wait for all responses
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Failed to process all RabbitMQ responses in time");

        long rabbitDuration = System.currentTimeMillis() - rabbitStart;
        double rabbitAvg = (double) rabbitDuration / REQUEST_COUNT;

        LOG.info("RabbitMQ API: {} requests in {}ms (avg: {}ms/request)",
                REQUEST_COUNT, rabbitDuration, rabbitAvg);

        // Compare performance
        LOG.info("Performance comparison:");
        LOG.info("Direct API avg latency: {}ms", directAvg);
        LOG.info("RabbitMQ avg latency: {}ms", rabbitAvg);
        LOG.info("Ratio (RabbitMQ/Direct): {}", rabbitAvg / directAvg);

        // We expect RabbitMQ to be slower due to serialization and messaging overhead
        assertTrue(rabbitAvg > directAvg, "Expected RabbitMQ to be slower than direct API calls");

        // But it shouldn't be excessively slower (e.g., not more than 10x slower)
        assertTrue(rabbitAvg / directAvg < 10,
                "RabbitMQ is excessively slower than direct API calls");
    }

    @Test
    void testThroughputScaling() {
        // Measure throughput with different concurrency levels
        int[] concurrencyLevels = {1, 5, 10, 20, 50};
        int requestsPerConcurrency = 100;

        for (int concurrency : concurrencyLevels) {
            // Create requests
            List<GetDataRequest> requests = createTestRequests(requestsPerConcurrency);

            // Warm up
            Flux.fromIterable(requests.subList(0, 5))
                    .flatMap(request ->
                            mockVisaClient.getData(request, UUID.randomUUID().toString()))
                    .collectList()
                    .block();

            // Measure throughput
            long start = System.currentTimeMillis();

            List<GetDataResponse> results = Flux.fromIterable(requests)
                    .flatMap(request ->
                                    mockVisaClient.getData(request, UUID.randomUUID().toString()),
                            concurrency) // Set concurrency level
                    .collectList()
                    .block(Duration.ofSeconds(30));

            long duration = System.currentTimeMillis() - start;

            // Calculate throughput
            double throughput = requestsPerConcurrency * 1000.0 / duration; // requests per second

            LOG.info("Concurrency level {}: processed {} requests in {}ms (throughput: {}/s)",
                    concurrency, requestsPerConcurrency, duration, String.format("%.2f", throughput));

            // Verify all requests were processed
            assertNotNull(results);
            assertEquals(requestsPerConcurrency, results.size());
        }
    }

    @Test
    void testMemoryUsage() {
        // Process a large number of requests to observe memory usage
        final int REQUEST_COUNT = 1000;
        final int BATCH_SIZE = 100;

        // Create requests
        List<GetDataRequestDto> requests = new ArrayList<>(REQUEST_COUNT);
        for (int i = 0; i < REQUEST_COUNT; i++) {
            requests.add(TestDataFactory.createGetDataRequestDto("mem-test-" + i));
        }

        // Record memory before test
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Request garbage collection
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        LOG.info("Memory usage before test: {}MB", memoryBefore / (1024 * 1024));

        // Process requests in batches
        AtomicInteger processedCount = new AtomicInteger(0);

        for (int i = 0; i < REQUEST_COUNT; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, REQUEST_COUNT);
            List<GetDataRequestDto> batch = requests.subList(i, end);

            Flux.fromIterable(batch)
                    .flatMap(request -> coordinator.getDataAsync(request))
                    .doOnNext(response -> processedCount.incrementAndGet())
                    .blockLast(Duration.ofSeconds(10));

            // Log progress
            LOG.info("Processed {}/{} requests", processedCount.get(), REQUEST_COUNT);
        }

        // Record memory after test
        System.gc(); // Request garbage collection
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        LOG.info("Memory usage after test: {}MB", memoryAfter / (1024 * 1024));
        LOG.info("Memory difference: {}MB", (memoryAfter - memoryBefore) / (1024 * 1024));

        // Verify all requests were processed
        assertEquals(REQUEST_COUNT, processedCount.get());

        // Memory usage should not grow excessively (e.g., less than 100MB)
        assertTrue((memoryAfter - memoryBefore) < 100 * 1024 * 1024,
                "Memory usage grew excessively");
    }

    // Helper methods

    private void setupMockResponses() {
        // Mock responses for getData
        when(mockVisaClient.getData(any(GetDataRequest.class), anyString()))
                .thenAnswer(invocation -> {
                    GetDataRequest request = invocation.getArgument(0);
                    String consumerId = request.consumerInformation().externalConsumerID();

                    // Simulate some processing delay (50ms)
                    return Mono.delay(Duration.ofMillis(50))
                            .then(Mono.just(MockResponseUtil.createMockGetDataResponse(consumerId)));
                });

        // Mock responses for enrollData
        when(mockVisaClient.enrollData(any(), anyString()))
                .thenAnswer(invocation -> {
                    // Simulate some processing delay (20ms)
                    return Mono.delay(Duration.ofMillis(20))
                            .then(Mono.just(RequestIdResponse.builder()
                                    .requestTraceId(UUID.randomUUID().toString())
                                    .build()));
                });
    }

    private List<GetDataRequest> createTestRequests(int count) {
        List<GetDataRequest> requests = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            requests.add(GetDataRequest.builder()
                    .intent(dev.example.visa.model.Intent.builder()
                            .type("PRODUCT_CODE")
                            .value("CLICK_TO_PAY")
                            .build())
                    .consumerInformation(dev.example.visa.model.ConsumerInformationIdRef.builder()
                            .externalConsumerID("perf-test-" + i)
                            .build())
                    .build());
        }

        return requests;
    }
}