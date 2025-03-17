package dev.example.visa.test;

import dev.example.visa.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 * Centralized to maintain consistency across tests.
 */
public class TestDataFactory {

    /**
     * Creates a standard test intent for Click to Pay.
     */
    public static Intent createClickToPayIntent() {
        return Intent.builder()
                .type("PRODUCT_CODE")
                .value("CLICK_TO_PAY")
                .build();
    }

    /**
     * Creates a sample consumer information with a random external ID.
     */
    public static ConsumerInformation createConsumerInformation() {
        return ConsumerInformation.builder()
                .firstName("John")
                .lastName("Doe")
                .countryCode("USA")
                .externalConsumerID(UUID.randomUUID().toString())
                .emails(Collections.singletonList("john.doe@example.com"))
                .locale("en_US")
                .phones(Collections.singletonList("16504005555"))
                .consent(createConsent())
                .build();
    }

    /**
     * Creates a sample consent object.
     */
    public static Consent createConsent() {
        return Consent.builder()
                .version("1.0")
                .presenter("Test Bank")
                .timeOfConsent("2023-01-01T12:00:00.000Z")
                .build();
    }

    /**
     * Creates a consumer reference with a random ID.
     */
    public static ConsumerInformationIdRef createConsumerInformationIdRef() {
        return ConsumerInformationIdRef.builder()
                .externalConsumerID(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Creates a list of payment instruments.
     */
    public static List<?> createPaymentInstruments() {
        List<Object> instruments = new ArrayList<>();
        instruments.add(createCardPaymentInstrument());
        return instruments;
    }

    /**
     * Creates a test card payment instrument.
     */
    public static CardPaymentInstrument createCardPaymentInstrument() {
        return CardPaymentInstrument.builder()
                .type("CARD")
                .nameOnCard("John Doe")
                .accountNumber("4111111111111111")
                .expirationDate("2030-01")
                .cardType("Visa Platinum")
                .issuerName("Test Bank")
                .billingAddress(createAddress())
                .build();
    }

    /**
     * Creates a test bank account payment instrument.
     */
    public static BankAccountPaymentInstrument createBankAccountPaymentInstrument() {
        return BankAccountPaymentInstrument.builder()
                .type("BANK_ACCOUNT")
                .accountName("John Doe")
                .accountNumber("1001001234")
                .accountNumberType("DEFAULT")
                .accountType("CHECKING")
                .bankCode("173")
                .bankName("Test Bank")
                .bankCodeType("DEFAULT")
                .countryCode("USA")
                .currencyCode("USD")
                .address(createAddress())
                .build();
    }

    /**
     * Creates a sample address.
     */
    public static Address createAddress() {
        return Address.builder()
                .addressLine1("123 Main St")
                .city("San Francisco")
                .state("CA")
                .postalCode("94105")
                .country("USA")
                .build();
    }

    /**
     * Creates a sample enroll data request.
     */
    public static EnrollDataRequest createEnrollDataRequest() {
        return EnrollDataRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformation())
                .paymentInstruments(createPaymentInstruments())
                .build();
    }

    /**
     * Creates a sample enroll payment instruments request.
     */
    public static EnrollPaymentInstrumentsRequest createEnrollPaymentInstrumentsRequest() {
        return EnrollPaymentInstrumentsRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .paymentInstruments(createPaymentInstruments())
                .build();
    }

    /**
     * Creates a sample get data request.
     */
    public static GetDataRequest createGetDataRequest() {
        return GetDataRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .build();
    }

    /**
     * Creates a sample manage consumer information request.
     */
    public static ManageConsumerInformationRequest createManageConsumerInformationRequest() {
        return ManageConsumerInformationRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformation())
                .build();
    }

    /**
     * Creates a sample manage payment instruments request.
     */
    public static ManagePaymentInstrumentsRequest createManagePaymentInstrumentsRequest() {
        return ManagePaymentInstrumentsRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .paymentInstruments(createPaymentInstruments())
                .build();
    }

    /**
     * Creates a sample delete consumer information request.
     */
    public static DeleteConsumerInformationRequest createDeleteConsumerInformationRequest() {
        return DeleteConsumerInformationRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .build();
    }

    /**
     * Creates a sample delete payment instruments request.
     */
    public static DeletePaymentInstrumentsRequest createDeletePaymentInstrumentsRequest() {
        return DeletePaymentInstrumentsRequest.builder()
                .intent(createClickToPayIntent())
                .consumerInformation(createConsumerInformationIdRef())
                .paymentInstruments(createCardPaymentInstrument())
                .build();
    }
}