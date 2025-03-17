package dev.example.visa.messaging;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"test"}, transactional = false)
@Property(name = "rabbitmq.host", value = "localhost")
@Property(name = "rabbitmq.port", value = "5672")
@Property(name = "rabbitmq.username", value = "guest")
@Property(name = "rabbitmq.password", value = "guest")
@Property(name = "visa.security.vault.enabled", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Test
    void testManageConsumerInformation() {
        // Arrange
        ManageConsumerInformationRequest request = createTestManageConsumerRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(messageHandler.manageConsumerInformation(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                    assertEquals("test-request-id", response.requestTraceId());
                })
                .verifyComplete();
    }

    @Test
    void testDeletePaymentInstruments() {
        // Arrange
        DeletePaymentInstrumentsRequest request = createTestDeletePaymentRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(messageHandler.deletePaymentInstruments(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                    assertEquals("test-request-id", response.requestTraceId());
                })
                .verifyComplete();
    }

    @Test
    void testRequestStatus() {
        // Arrange
        String requestTraceId = "test-trace-id";
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(messageHandler.requestStatus(requestTraceId, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("COMPLETED", response.status());
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

    private ManageConsumerInformationRequest createTestManageConsumerRequest() {
        return ManageConsumerInformationRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(ConsumerInformation.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .countryCode("USA")
                        .externalConsumerID(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    private DeletePaymentInstrumentsRequest createTestDeletePaymentRequest() {
        CardPaymentInstrument cardInstrument = CardPaymentInstrument.builder()
                .type("CARD")
                .accountNumber("4111111145551140")
                .build();

        return DeletePaymentInstrumentsRequest.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .paymentInstruments(cardInstrument)
                .consumerInformation(ConsumerInformationIdRef.builder()
                        .externalConsumerID(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    // Mock beans
    @MockBean(VisaClickToPayClient.class)
    VisaClickToPayClient visaClient() {
        VisaClickToPayClient mock = mock(VisaClickToPayClient.class);

        // Set up test behavior for all methods
        when(mock.enrollPaymentInstruments(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        when(mock.enrollData(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        when(mock.getRequestStatus(any(), any())).thenReturn(
                Mono.just(RequestStatusResponse.builder().status("COMPLETED").build()));

        when(mock.managePaymentInstruments(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        when(mock.manageConsumerInformation(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        when(mock.deleteConsumerInformation(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        when(mock.deletePaymentInstruments(any(), any())).thenReturn(
                Mono.just(RequestIdResponse.builder().requestTraceId("test-request-id").build()));

        // Set up getData behavior with more specific response
        when(mock.getData(any(), any())).thenAnswer(invocation -> {
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

        return mock;
    }

    // Helper methods
    private <T> T any() {
        return null; // Mockito will substitute this with any() matcher
    }
}