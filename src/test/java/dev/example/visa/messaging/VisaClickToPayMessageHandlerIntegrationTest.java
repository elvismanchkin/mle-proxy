package dev.example.visa.messaging;

import dev.example.visa.model.*;
import dev.example.visa.test.BaseIntegrationTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for VisaClickToPayMessageHandler.
 * Uses RabbitMQ TestContainer for actual messaging interaction,
 * while still mocking the Visa API client.
 */
class VisaClickToPayMessageHandlerIntegrationTest extends BaseIntegrationTest {

    @Inject
    VisaClickToPayProducer producer;

    @Test
    void testEnrollDataViaRabbitMQ() {
        // Arrange
        EnrollDataRequest request = createTestEnrollDataRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(producer.enrollData(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                })
                .verifyComplete();
    }

    @Test
    void testGetDataViaRabbitMQ() {
        // Arrange
        GetDataRequest request = createTestGetDataRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(producer.getData(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.data());
                })
                .verifyComplete();
    }

    @Test
    void testDeletePaymentInstrumentsViaRabbitMQ() {
        // Arrange
        DeletePaymentInstrumentsRequest request = createTestDeletePaymentInstrumentsRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(producer.deletePaymentInstruments(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                })
                .verifyComplete();
    }

    // Helper methods to create test data

    private EnrollDataRequest createTestEnrollDataRequest() {
        return EnrollDataRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformation())
                .paymentInstruments(createPaymentInstruments())
                .build();
    }

    private GetDataRequest createTestGetDataRequest() {
        return GetDataRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .build();
    }

    private DeletePaymentInstrumentsRequest createTestDeletePaymentInstrumentsRequest() {
        return DeletePaymentInstrumentsRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .paymentInstruments(createCardPaymentInstrument())
                .build();
    }

    private Intent createIntent() {
        return Intent.builder()
                .type("PRODUCT_CODE")
                .value("CLICK_TO_PAY")
                .build();
    }

    private ConsumerInformation createConsumerInformation() {
        return ConsumerInformation.builder()
                .firstName("John")
                .lastName("Doe")
                .countryCode("USA")
                .externalConsumerID(UUID.randomUUID().toString())
                .build();
    }

    private ConsumerInformationIdRef createConsumerInformationIdRef() {
        return ConsumerInformationIdRef.builder()
                .externalConsumerID(UUID.randomUUID().toString())
                .build();
    }

    private List<?> createPaymentInstruments() {
        List<Object> instruments = new ArrayList<>();
        instruments.add(createCardPaymentInstrument());
        return instruments;
    }

    private CardPaymentInstrument createCardPaymentInstrument() {
        return CardPaymentInstrument.builder()
                .type("CARD")
                .nameOnCard("John Doe")
                .accountNumber("4111111111111111")
                .expirationDate("2030-01")
                .billingAddress(createAddress())
                .build();
    }

    private Address createAddress() {
        return Address.builder()
                .addressLine1("123 Main St")
                .city("San Francisco")
                .state("CA")
                .postalCode("94105")
                .country("USA")
                .build();
    }
}