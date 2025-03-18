package dev.example.visa.integration;

import dev.example.visa.dto.AddressDto;
import dev.example.visa.dto.DeleteConsumerInformationRequestDto;
import dev.example.visa.dto.DeletePaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollPaymentInstrumentsRequestDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.dto.ManageConsumerInformationRequestDto;
import dev.example.visa.dto.ManagePaymentInstrumentsRequestDto;

import java.util.List;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    /**
     * Creates a standard enrollment request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated enrollment request
     */
    public static EnrollDataRequestDto createEnrollDataRequestDto(String consumerId) {
        return EnrollDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .countryCode("USA")
                .emails(List.of("john.doe@example.com"))
                .phones(List.of("16505551234"))
                .locale("en_US")
                .consentVersion("1.0")
                .consentPresenter("Test Bank")
                .consentTimestamp("2023-01-01T12:00:00.000Z")
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2025-12")
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("123 Main St")
                        .addressLine2("Apt 4B")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    /**
     * Creates a standard payment enrollment request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated payment enrollment request
     */
    public static EnrollPaymentInstrumentsRequestDto createEnrollPaymentInstrumentsRequestDto(String consumerId) {
        return EnrollPaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2025-12")
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("123 Main St")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    /**
     * Creates a bank account enrollment request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated bank account enrollment request
     */
    public static EnrollPaymentInstrumentsRequestDto createBankEnrollmentRequestDto(String consumerId) {
        return EnrollPaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .paymentType("BANK_ACCOUNT")
                .accountNumber("12345678901234")
                .accountName("John Doe")
                .accountType("CHECKING")
                .bankName("Test Bank")
                .bankCode("123456789")
                .branchCode("001")
                .bankCodeType("DEFAULT")
                .currencyCode("USD")
                .accountNumberType("DEFAULT")
                .build();
    }

    /**
     * Creates a payment management request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated payment management request
     */
    public static ManagePaymentInstrumentsRequestDto createManagePaymentInstrumentsRequestDto(String consumerId) {
        return ManagePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2026-12") // Updated expiration date
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("456 New St") // Updated address
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    /**
     * Creates a consumer management request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated consumer management request
     */
    public static ManageConsumerInformationRequestDto createManageConsumerInformationRequestDto(String consumerId) {
        return ManageConsumerInformationRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .countryCode("USA")
                .emails(List.of("new.email@example.com")) // Updated email
                .phones(List.of("16505551234"))
                .locale("en_US")
                .status("ACTIVE")
                .build();
    }

    /**
     * Creates a consumer deletion request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated consumer deletion request
     */
    public static DeleteConsumerInformationRequestDto createDeleteConsumerInformationRequestDto(String consumerId) {
        return DeleteConsumerInformationRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .build();
    }

    /**
     * Creates a payment deletion request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated payment deletion request
     */
    public static DeletePaymentInstrumentsRequestDto createDeletePaymentInstrumentsRequestDto(String consumerId) {
        return DeletePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .build();
    }

    /**
     * Creates a data retrieval request DTO for testing.
     *
     * @param consumerId The consumer ID to use
     * @return A populated data retrieval request
     */
    public static GetDataRequestDto createGetDataRequestDto(String consumerId) {
        return GetDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerId)
                .build();
    }
}