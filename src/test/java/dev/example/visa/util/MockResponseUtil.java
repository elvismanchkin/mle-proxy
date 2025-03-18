package dev.example.visa.util;

import dev.example.visa.model.Address;
import dev.example.visa.model.BankAccountPaymentInstrument;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.Consent;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.RequestStatusResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for creating mock API responses for testing.
 */
public class MockResponseUtil {

    /**
     * Creates a complete mock GetDataResponse for testing.
     *
     * @param consumerId The consumer ID to use in the response
     * @return A populated GetDataResponse object
     */
    public static GetDataResponse createMockGetDataResponse(String consumerId) {
        ConsumerInformation consumerInfo = ConsumerInformation.builder()
                .externalConsumerID(consumerId)
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .countryCode("USA")
                .emails(Collections.singletonList("john.doe@example.com"))
                .phones(Collections.singletonList("16505551234"))
                .locale("en_US")
                .consent(Consent.builder()
                        .version("1.0")
                        .presenter("Test Bank")
                        .timeOfConsent("2023-01-01T12:00:00.000Z")
                        .build())
                .build();

        CardPaymentInstrument cardInstrument = CardPaymentInstrument.builder()
                .type("CARD")
                .cardType("Visa")
                .issuerName("Test Bank")
                .nameOnCard("John A Doe")
                .accountNumber("4111111111111111")
                .expirationDate("2025-12")
                .billingAddress(Address.builder()
                        .addressLine1("123 Main St")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();

        BankAccountPaymentInstrument bankInstrument = BankAccountPaymentInstrument.builder()
                .type("BANK_ACCOUNT")
                .accountNumber("12345678901234")
                .accountName("John A Doe")
                .accountType("CHECKING")
                .bankName("Test Bank")
                .countryCode("USA")
                .currencyCode("USD")
                .accountNumberType("DEFAULT")
                .build();

        GetDataResponse.DataItem dataItem = GetDataResponse.DataItem.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .consumerInformation(consumerInfo)
                .paymentInstruments(List.of(cardInstrument, bankInstrument))
                .build();

        return GetDataResponse.builder()
                .data(Collections.singletonList(dataItem))
                .build();
    }

    /**
     * Creates a mock RequestStatusResponse for testing.
     *
     * @param status The status to set in the response (e.g., "COMPLETED", "IN_PROGRESS")
     * @param consumerId The consumer ID to use in the response
     * @return A populated RequestStatusResponse object
     */
    public static RequestStatusResponse createMockRequestStatusResponse(String status, String consumerId) {
        RequestStatusResponse.StatusDetail detail = RequestStatusResponse.StatusDetail.builder()
                .intent(Intent.builder()
                        .type("PRODUCT_CODE")
                        .value("CLICK_TO_PAY")
                        .build())
                .status("SUCCESS")
                .errorDetails(Collections.emptyList())
                .build();

        return RequestStatusResponse.builder()
                .status(status)
                .details(Collections.singletonList(detail))
                .consumerInformation(dev.example.visa.model.ConsumerInformationIdRef.builder()
                        .externalConsumerID(consumerId)
                        .build())
                .build();
    }

    /**
     * Creates a mock error detail for testing error responses.
     *
     * @param field The field with the error
     * @param reason The error reason
     * @param message The error message
     * @return A populated ErrorDetail object
     */
    public static RequestStatusResponse.ErrorDetail createMockErrorDetail(String field, String reason, String message) {
        return RequestStatusResponse.ErrorDetail.builder()
                .field(field)
                .reason(reason)
                .message(message)
                .build();
    }

    /**
     * Generates a random request trace ID for testing.
     *
     * @return A random request trace ID
     */
    public static String generateRequestTraceId() {
        return "test-trace-" + UUID.randomUUID();
    }
}