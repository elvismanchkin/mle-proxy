package dev.example.visa.messaging;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.RequestIdResponse;
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

@MicronautTest
class VisaClickToPayMessageHandlerTest {

    @Inject
    VisaClickToPayMessageHandler messageHandler;

    @Test
    void testEnrollData() {
        // Arrange
        EnrollDataRequest request = createTestEnrollDataRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(messageHandler.enrollData(request, correlationId))
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
        StepVerifier.create(messageHandler.getData(request, correlationId))
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

    // Mock implementation of the Visa client for testing
    @Singleton
    @Replaces(VisaClickToPayClient.class)
    static class MockVisaClient implements VisaClickToPayClient {

        @Override
        public Mono<RequestIdResponse> enrollPaymentInstruments(dev.example.visa.model.EnrollPaymentInstrumentsRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<RequestIdResponse> enrollData(EnrollDataRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<dev.example.visa.model.RequestStatusResponse> getRequestStatus(String requestTraceId, String correlationId) {
            return Mono.just(dev.example.visa.model.RequestStatusResponse.builder()
                    .status("COMPLETED")
                    .build());
        }

        @Override
        public Mono<RequestIdResponse> managePaymentInstruments(dev.example.visa.model.ManagePaymentInstrumentsRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<RequestIdResponse> manageConsumerInformation(dev.example.visa.model.ManageConsumerInformationRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<RequestIdResponse> deleteConsumerInformation(dev.example.visa.model.DeleteConsumerInformationRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<RequestIdResponse> deletePaymentInstruments(dev.example.visa.model.DeletePaymentInstrumentsRequest request, String correlationId) {
            return Mono.just(RequestIdResponse.builder()
                    .requestTraceId("test-request-id")
                    .build());
        }

        @Override
        public Mono<GetDataResponse> getData(GetDataRequest request, String correlationId) {
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
        }
    }
}