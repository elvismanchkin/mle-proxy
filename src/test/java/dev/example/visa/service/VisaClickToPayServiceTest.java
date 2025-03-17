package dev.example.visa.service;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.RequestIdResponse;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test focused on the service layer with mocked dependencies
 */
@MicronautTest(environments = {"test"}, startApplication = false, rebuildContext = true)
@Property(name = "otel.sdk.disabled", value = "true")
@Property(name = "otel.exporter.otlp.enabled", value = "false")
@Property(name = "visa.security.vault.enabled", value = "false")
@Property(name = "rabbitmq.enabled", value = "false")
class VisaClickToPayServiceTest {

    @Inject
    VisaClickToPayService service;

    @Factory
    static class MockFactory {
        @Singleton
        @Primary
        @Replaces(VisaClickToPayClient.class)
        VisaClickToPayClient mockVisaClient() {
            VisaClickToPayClient mockClient = mock(VisaClickToPayClient.class);

            // Configure the mock behaviors
            when(mockClient.enrollData(any(), anyString()))
                    .thenReturn(Mono.just(RequestIdResponse.builder()
                            .requestTraceId("test-request-id")
                            .build()));

            when(mockClient.getData(any(), anyString()))
                    .thenAnswer(invocation -> {
                        GetDataRequest request = invocation.getArgument(0);

                        GetDataResponse.DataItem item = GetDataResponse.DataItem.builder()
                                .intent(request.intent())
                                .consumerInformation(ConsumerInformation.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .countryCode("USA")
                                        .externalConsumerID(request.consumerInformation().externalConsumerID())
                                        .build())
                                .build();

                        return Mono.just(GetDataResponse.builder()
                                .data(List.of(item))
                                .build());
                    });

            return mockClient;
        }
    }

    @Test
    void testEnrollData() {
        // Arrange
        EnrollDataRequest request = createTestEnrollDataRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(service.enrollData(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                    assertEquals("test-request-id", response.requestTraceId());
                })
                .verifyComplete();
    }

    @Test
    void testGetData() {
        // Arrange
        GetDataRequest request = createTestGetDataRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(service.getData(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.data());
                    assertEquals(1, response.data().size());
                    assertEquals("John", response.data().get(0).consumerInformation().firstName());
                })
                .verifyComplete();
    }

    // Helper methods to create test data
    private EnrollDataRequest createTestEnrollDataRequest() {
        return EnrollDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformation.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .countryCode("USA")
                        .externalConsumerID(UUID.randomUUID().toString())
                        .build())
                .paymentInstruments(Collections.emptyList())
                .build();
    }

    private GetDataRequest createTestGetDataRequest() {
        return GetDataRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(UUID.randomUUID().toString())
                        .build())
                .build();
    }
}