package dev.example.visa.integration;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.service.VisaClickToPayCoordinator;
import dev.example.visa.util.MockResponseUtil;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * End-to-end test for a complete enrollment and verification flow.
 */
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisaClickToPayEndToEndTest {
    private static final Logger LOG = LoggerFactory.getLogger(VisaClickToPayEndToEndTest.class);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @Inject
    ApplicationContext context;

    @Inject
    VisaClickToPayClient mockVisaClient;

    @Inject
    VisaClickToPayCoordinator coordinator;

    @Test
    void testCompleteEnrollmentFlow() throws Exception {
        String testConsumerId = "test-consumer-" + UUID.randomUUID();
        String requestTraceId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        // Set up the mocks for a complete flow
        setupMockResponses(requestTraceId, testConsumerId);

        // Step 1: Enroll a consumer
        EnrollDataRequestDto enrollRequest = TestDataFactory.createEnrollDataRequestDto(testConsumerId);

        // Use the coordinator to perform the enrollment
        Mono<RequestStatusResponseDto> enrollmentResult = coordinator.enrollDataAsync(enrollRequest)
                .flatMap(response -> {
                    // Verify enrollment response
                    assertNotNull(response);
                    assertEquals("SUCCESS", response.status());
                    assertEquals(requestTraceId, response.requestId());

                    // Step 2: Check status of the enrollment
                    return coordinator.getRequestStatusAsync(requestTraceId);
                });

        // Execute the flow and verify
        RequestStatusResponseDto statusResponse = enrollmentResult.block(Duration.of(10, ChronoUnit.SECONDS));

        // Verify the status response
        assertNotNull(statusResponse);
        assertEquals("COMPLETED", statusResponse.status());

        // Step 3: Get consumer data to verify enrollment
        GetDataRequestDto getDataRequest = GetDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(testConsumerId)
                .build();

        var dataResponse = coordinator.getDataAsync(getDataRequest).block(Duration.of(10, ChronoUnit.SECONDS));

        // Verify the data response
        assertNotNull(dataResponse);
        assertEquals("SUCCESS", dataResponse.status());
        assertEquals(testConsumerId, dataResponse.consumerId());
        assertNotNull(dataResponse.consumerInfo());
        assertNotNull(dataResponse.paymentInstruments());
        assertEquals(2, dataResponse.paymentInstruments().size());
    }

    /**
     * Set up the mock responses for each step of the flow.
     */
    private void setupMockResponses(String requestTraceId, String consumerId) {
        // Mock responses for enroll data
        when(mockVisaClient.enrollData(any(EnrollDataRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder()
                        .requestTraceId(requestTraceId)
                        .build()));

        // Mock responses for request status
        when(mockVisaClient.getRequestStatus(anyString(), anyString()))
                .thenReturn(Mono.just(MockResponseUtil.createMockRequestStatusResponse(
                        "COMPLETED", consumerId)));

        // Mock responses for get data
        when(mockVisaClient.getData(any(), anyString()))
                .thenReturn(Mono.just(MockResponseUtil.createMockGetDataResponse(consumerId)));
    }
}