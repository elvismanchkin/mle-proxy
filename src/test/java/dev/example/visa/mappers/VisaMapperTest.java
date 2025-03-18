package dev.example.visa.mappers;

import dev.example.visa.dto.AddressDto;
import dev.example.visa.dto.ConsumerDataResponseDto;
import dev.example.visa.dto.DeletePaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.ErrorInfoDto;
import dev.example.visa.model.Address;
import dev.example.visa.model.BankAccountPaymentInstrument;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.Consent;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.ErrorResponse;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.RequestIdResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the unified Visa mapper.
 */
class VisaMapperTest {

    private final VisaMapper mapper = Mappers.getMapper(VisaMapper.class);

    @Test
    void testAddressMapping() {
        // Create DTO object
        AddressDto dto = AddressDto.builder()
                .city("San Francisco")
                .state("CA")
                .country("USA")
                .postalCode("94105")
                .addressLine1("123 Market St")
                .addressLine2("Suite 456")
                .addressLine3("Floor 7")
                .build();

        // Map to model
        Address model = mapper.mapToAddress(dto);

        // Verify
        assertNotNull(model);
        assertEquals("San Francisco", model.city());
        assertEquals("CA", model.state());
        assertEquals("USA", model.country());
        assertEquals("94105", model.postalCode());
        assertEquals("123 Market St", model.addressLine1());
        assertEquals("Suite 456", model.addressLine2());
        assertEquals("Floor 7", model.addressLine3());

        // Map back to DTO
        AddressDto roundTripped = mapper.mapToAddressDto(model);

        // Verify round trip
        assertNotNull(roundTripped);
        assertEquals(dto.city(), roundTripped.city());
        assertEquals(dto.state(), roundTripped.state());
        assertEquals(dto.country(), roundTripped.country());
        assertEquals(dto.postalCode(), roundTripped.postalCode());
        assertEquals(dto.addressLine1(), roundTripped.addressLine1());
        assertEquals(dto.addressLine2(), roundTripped.addressLine2());
        assertEquals(dto.addressLine3(), roundTripped.addressLine3());
    }

    @Test
    void testIntentCreation() {
        // Create intent
        Intent intent = mapper.createIntent("PRODUCT_CODE", "CLICK_TO_PAY");

        // Verify
        assertNotNull(intent);
        assertEquals("PRODUCT_CODE", intent.type());
        assertEquals("CLICK_TO_PAY", intent.value());

        // Test null handling
        assertNull(mapper.createIntent(null, "value"));
        assertNull(mapper.createIntent("type", null));
    }

    @Test
    void testConsentCreation() {
        // Create consent
        Consent consent = mapper.createConsent("1.0", "Bank A", "2023-05-01T12:00:00.000Z");

        // Verify
        assertNotNull(consent);
        assertEquals("1.0", consent.version());
        assertEquals("Bank A", consent.presenter());
        assertEquals("2023-05-01T12:00:00.000Z", consent.timeOfConsent());

        // Test null handling
        assertNull(mapper.createConsent("1.0", "Bank A", null));
    }

    @Test
    void testConsumerIdRefCreation() {
        // Create consumer ID ref
        ConsumerInformationIdRef ref = mapper.createConsumerIdRef("user-123");

        // Verify
        assertNotNull(ref);
        assertEquals("user-123", ref.externalConsumerID());

        // Test null handling
        assertNull(mapper.createConsumerIdRef(null));
    }

    @Test
    void testEnrollDataRequestMapping() {
        // Create DTO
        EnrollDataRequestDto dto = EnrollDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId("user-123")
                .firstName("John")
                .lastName("Doe")
                .countryCode("USA")
                .emails(Collections.singletonList("john@example.com"))
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .expirationDate("2025-12")
                .nameOnCard("John Doe")
                .billingAddress(AddressDto.builder()
                        .city("San Francisco")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();

        // Map to model
        EnrollDataRequest model = mapper.mapToEnrollDataRequest(dto);

        // Verify
        assertNotNull(model);
        assertNotNull(model.intent());
        assertEquals("PRODUCT_CODE", model.intent().type());
        assertEquals("CLICK_TO_PAY", model.intent().value());

        assertNotNull(model.consumerInformation());
        assertEquals("John", model.consumerInformation().firstName());
        assertEquals("Doe", model.consumerInformation().lastName());
        assertEquals("USA", model.consumerInformation().countryCode());
        assertEquals(1, model.consumerInformation().emails().size());
        assertEquals("john@example.com", model.consumerInformation().emails().get(0));

        assertNotNull(model.paymentInstruments());
        assertEquals(1, model.paymentInstruments().size());
        assertInstanceOf(CardPaymentInstrument.class, model.paymentInstruments().get(0));

        CardPaymentInstrument card = (CardPaymentInstrument) model.paymentInstruments().get(0);
        assertEquals("CARD", card.type());
        assertEquals("4111111111111111", card.accountNumber());
        assertEquals("2025-12", card.expirationDate());
        assertEquals("John Doe", card.nameOnCard());
        assertNotNull(card.billingAddress());
        assertEquals("USA", card.billingAddress().country());
    }

    @Test
    void testBankAccountMapping() {
        // Create DTO
        EnrollDataRequestDto dto = EnrollDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId("user-123")
                .paymentType("BANK_ACCOUNT")
                .accountNumber("12345678")
                .accountName("John Doe")
                .accountType("CHECKING")
                .bankName("Test Bank")
                .currencyCode("USD")
                .accountNumberType("DEFAULT")
                .countryCode("USA")
                .build();

        // Map to model
        EnrollDataRequest model = mapper.mapToEnrollDataRequest(dto);

        // Verify
        assertNotNull(model);
        assertNotNull(model.paymentInstruments());
        assertEquals(1, model.paymentInstruments().size());
        assertInstanceOf(BankAccountPaymentInstrument.class, model.paymentInstruments().get(0));

        BankAccountPaymentInstrument bank = (BankAccountPaymentInstrument) model.paymentInstruments().get(0);
        assertEquals("BANK_ACCOUNT", bank.type());
        assertEquals("12345678", bank.accountNumber());
        assertEquals("John Doe", bank.accountName());
        assertEquals("CHECKING", bank.accountType());
        assertEquals("Test Bank", bank.bankName());
        assertEquals("USD", bank.currencyCode());
        assertEquals("DEFAULT", bank.accountNumberType());
        assertEquals("USA", bank.countryCode());
    }

    @Test
    void testErrorResponseMapping() {
        // Create error model
        ErrorResponse.ErrorDetail detail = ErrorResponse.ErrorDetail.builder()
                .reason("ValidationError")
                .location("cardNumber")
                .build();

        ErrorResponse error = ErrorResponse.builder()
                .reason("InvalidParameter")
                .message("Invalid card number")
                .details(Collections.singletonList(detail))
                .build();

        // Map to DTO
        ErrorInfoDto errorDto = mapper.mapToErrorInfo(error);

        // Verify
        assertNotNull(errorDto);
        assertEquals("InvalidParameter", errorDto.reason());
        assertEquals("Invalid card number", errorDto.message());
        assertNotNull(errorDto.details());
        assertEquals(1, errorDto.details().size());
        assertEquals("ValidationError", errorDto.details().get(0).reason());
        assertEquals("cardNumber", errorDto.details().get(0).field());
    }

    @Test
    void testRequestIdResponseMapping() {
        // Create model
        RequestIdResponse response = RequestIdResponse.builder()
                .requestTraceId("trace-123")
                .build();

        // Map to DTO
        EnrollmentResponseDto dto = mapper.mapToEnrollmentResponse(response);

        // Verify
        assertNotNull(dto);
        assertEquals("SUCCESS", dto.status());
        assertEquals("trace-123", dto.requestId());
        assertNull(dto.error());
    }

    @Test
    void testGetDataResponseMapping() {
        // Create consumer info
        ConsumerInformation consumerInfo = ConsumerInformation.builder()
                .externalConsumerID("user-123")
                .firstName("John")
                .lastName("Doe")
                .emails(Collections.singletonList("john@example.com"))
                .countryCode("USA")
                .build();

        // Create card payment
        CardPaymentInstrument card = CardPaymentInstrument.builder()
                .type("CARD")
                .accountNumber("4111111111111111")
                .nameOnCard("John Doe")
                .expirationDate("2025-12")
                .cardType("Visa")
                .build();

        // Create data item
        GetDataResponse.DataItem item = GetDataResponse.DataItem.builder()
                .intent(Intent.builder().type("PRODUCT_CODE").value("CLICK_TO_PAY").build())
                .consumerInformation(consumerInfo)
                .paymentInstruments(Collections.singletonList(card))
                .build();

        // Create response
        GetDataResponse response = GetDataResponse.builder()
                .data(Collections.singletonList(item))
                .build();

        // Map to DTO
        ConsumerDataResponseDto dto = mapper.mapToConsumerDataResponse(response);

        // Verify
        assertNotNull(dto);
        assertEquals("SUCCESS", dto.status());
        assertEquals("user-123", dto.consumerId());

        assertNotNull(dto.consumerInfo());
        assertEquals("John", dto.consumerInfo().firstName());
        assertEquals("Doe", dto.consumerInfo().lastName());

        assertNotNull(dto.paymentInstruments());
        assertEquals(1, dto.paymentInstruments().size());
        assertEquals("CARD", dto.paymentInstruments().get(0).type());
        assertEquals("4111111111111111", dto.paymentInstruments().get(0).accountNumber());
        assertEquals("John Doe", dto.paymentInstruments().get(0).nameOnCard());
        assertEquals("2025-12", dto.paymentInstruments().get(0).expirationDate());
        assertEquals("Visa", dto.paymentInstruments().get(0).cardType());
    }

    @Test
    void testMapPaymentInstrumentFromMap() {
        // Create a map representing a card payment
        Map<String, Object> cardMap = Map.of(
                "type", "CARD",
                "accountNumber", "4111111111111111",
                "nameOnCard", "John Doe",
                "cardType", "Visa",
                "expirationDate", "2025-12"
        );

        // Map to DTO
        ConsumerDataResponseDto.PaymentInstrumentInfoDto dto = mapper.mapPaymentInstrument(cardMap);

        // Verify
        assertNotNull(dto);
        assertEquals("CARD", dto.type());
        assertEquals("4111111111111111", dto.accountNumber());
        assertEquals("John Doe", dto.nameOnCard());
        assertEquals("Visa", dto.cardType());
        assertEquals("2025-12", dto.expirationDate());
    }

    @Test
    void testDeletePaymentInstrumentMapping() {
        // Create delete card request DTO
        DeletePaymentInstrumentsRequestDto cardDto = DeletePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId("user-123")
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .build();

        // Map to model
        DeletePaymentInstrumentsRequest cardRequest = mapper.mapToDeletePaymentInstrumentsRequest(cardDto);

        // Verify
        assertNotNull(cardRequest);
        assertNotNull(cardRequest.intent());
        assertEquals("PRODUCT_CODE", cardRequest.intent().type());
        assertNotNull(cardRequest.paymentInstruments());
        assertInstanceOf(VisaMapper.DeleteCardPaymentInstrument.class, cardRequest.paymentInstruments());

        VisaMapper.DeleteCardPaymentInstrument deleteCard =
                (VisaMapper.DeleteCardPaymentInstrument) cardRequest.paymentInstruments();
        assertEquals("CARD", deleteCard.type());
        assertEquals("4111111111111111", deleteCard.accountNumber());

        // Create delete bank account request DTO
        DeletePaymentInstrumentsRequestDto bankDto = DeletePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId("user-123")
                .paymentType("BANK_ACCOUNT")
                .accountNumber("12345678")
                .accountName("John Doe")
                .accountNumberType("DEFAULT")
                .build();

        // Map to model
        DeletePaymentInstrumentsRequest bankRequest = mapper.mapToDeletePaymentInstrumentsRequest(bankDto);

        // Verify
        assertNotNull(bankRequest);
        assertNotNull(bankRequest.paymentInstruments());
        assertInstanceOf(VisaMapper.DeleteBankAccountPaymentInstrument.class, bankRequest.paymentInstruments());

        VisaMapper.DeleteBankAccountPaymentInstrument deleteBank =
                (VisaMapper.DeleteBankAccountPaymentInstrument) bankRequest.paymentInstruments();
        assertEquals("BANK_ACCOUNT", deleteBank.type());
        assertEquals("12345678", deleteBank.accountNumber());
        assertEquals("John Doe", deleteBank.accountName());
        assertEquals("DEFAULT", deleteBank.accountNumberType());
    }

    @Test
    void testCorrelationIdGeneration() {
        String id1 = mapper.generateCorrelationId();
        String id2 = mapper.generateCorrelationId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertEquals(36, id1.length()); // UUID length
    }
}