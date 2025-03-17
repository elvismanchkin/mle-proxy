package dev.example.visa.service;

import dev.example.visa.model.Address;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.EnrollPaymentInstrumentsRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.Intent;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.test.BaseUnitTest;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for VisaClickToPayService.
 * Uses mocked dependencies for isolated testing.
 */
@ExtendWith(MicronautJunit5Extension.class)
class VisaClickToPayServiceTest extends BaseUnitTest {

    @Inject
    VisaClickToPayService service;

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
                })
                .verifyComplete();
    }

    @Test
    void testEnrollPaymentInstruments() {
        // Arrange
        EnrollPaymentInstrumentsRequest request = createTestEnrollPaymentInstrumentsRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(service.enrollPaymentInstruments(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
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
                })
                .verifyComplete();
    }

    @Test
    void testManageConsumerInformation() {
        // Arrange
        ManageConsumerInformationRequest request = createTestManageConsumerInformationRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(service.manageConsumerInformation(request, correlationId))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.requestTraceId());
                })
                .verifyComplete();
    }

    @Test
    void testDeletePaymentInstruments() {
        // Arrange
        DeletePaymentInstrumentsRequest request = createTestDeletePaymentInstrumentsRequest();
        String correlationId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(service.deletePaymentInstruments(request, correlationId))
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

    private EnrollPaymentInstrumentsRequest createTestEnrollPaymentInstrumentsRequest() {
        return EnrollPaymentInstrumentsRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .paymentInstruments(createPaymentInstruments())
                .build();
    }

    private GetDataRequest createTestGetDataRequest() {
        return GetDataRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .build();
    }

    private ManageConsumerInformationRequest createTestManageConsumerInformationRequest() {
        return ManageConsumerInformationRequest.builder()
                .intent(createIntent())
                .consumerInformation(createConsumerInformation())
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