package dev.example.visa.mappers;

import dev.example.visa.dto.AddressDto;
import dev.example.visa.dto.ConsumerDataResponseDto;
import dev.example.visa.dto.DeleteConsumerInformationRequestDto;
import dev.example.visa.dto.DeletePaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollPaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.ErrorInfoDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.dto.ManageConsumerInformationRequestDto;
import dev.example.visa.dto.ManagePaymentInstrumentsRequestDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.model.Address;
import dev.example.visa.model.BankAccountPaymentInstrument;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.Consent;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.DeleteConsumerInformationRequest;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.EnrollPaymentInstrumentsRequest;
import dev.example.visa.model.ErrorResponse;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.model.ManagePaymentInstrumentsRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mapstruct.MappingConstants.ComponentModel.JSR330;

/**
 * Unified mapper for Visa API requests and responses.
 * Uses consistent mapping patterns throughout.
 */
@Mapper(componentModel = JSR330,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = false))
public interface VisaMapper {

    // ===============================================
    // Common Helper Methods
    // ===============================================

    /**
     * Generates a correlation ID for tracing requests.
     */
    default String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Safely extracts string from a map.
     */
    default String getMapString(java.util.Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    // ===============================================
    // Basic Model/DTO Mappings
    // ===============================================

    /**
     * Maps DTO address to model address.
     */
    Address mapToAddress(AddressDto addressDto);

    /**
     * Maps model address to DTO address.
     */
    AddressDto mapToAddressDto(Address address);

    /**
     * Creates an Intent object from type and value.
     */
    default Intent createIntent(String type, String value) {
        if (type == null || value == null) {
            return null;
        }
        return Intent.builder()
                .type(type)
                .value(value)
                .build();
    }

    /**
     * Creates a Consent object from its components.
     */
    default Consent createConsent(String version, String presenter, String timeOfConsent) {
        if (timeOfConsent == null) {
            return null;
        }
        return Consent.builder()
                .version(version)
                .presenter(presenter)
                .timeOfConsent(timeOfConsent)
                .build();
    }

    /**
     * Creates a ConsumerInformationIdRef from an external consumer ID.
     */
    default ConsumerInformationIdRef createConsumerIdRef(String externalConsumerId) {
        if (externalConsumerId == null) {
            return null;
        }
        return ConsumerInformationIdRef.builder()
                .externalConsumerID(externalConsumerId)
                .build();
    }

    // ===============================================
    // Error Response Mappings
    // ===============================================

    /**
     * Maps Visa API error to DTO error info.
     */
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "details", source = "details")
    ErrorInfoDto mapToErrorInfo(ErrorResponse errorResponse);

    /**
     * Maps error detail from Visa API to DTO error detail.
     */
    @Mapping(target = "field", source = "location")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", ignore = true)
    ErrorInfoDto.ErrorDetailDto mapToErrorDetail(ErrorResponse.ErrorDetail detail);

    /**
     * Creates a standardized error enrollment response.
     */
    default EnrollmentResponseDto createErrorEnrollmentResponse(ErrorResponse errorResponse) {
        return EnrollmentResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }

    /**
     * Creates a standardized error status response.
     */
    default RequestStatusResponseDto createErrorStatusResponse(ErrorResponse errorResponse) {
        return RequestStatusResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }

    /**
     * Creates a standardized error data response.
     */
    default ConsumerDataResponseDto createErrorDataResponse(ErrorResponse errorResponse) {
        return ConsumerDataResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }

    // ===============================================
    // Consumer Information Mappings
    // ===============================================

    /**
     * Maps enrollment request DTO to consumer information model.
     */
    default ConsumerInformation createConsumerInformation(EnrollDataRequestDto requestDto) {
        if (requestDto == null) return null;

        return ConsumerInformation.builder()
                .externalConsumerID(requestDto.consumerId())
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .middleName(requestDto.middleName())
                .countryCode(requestDto.countryCode())
                .emails(requestDto.emails())
                .phones(requestDto.phones())
                .locale(requestDto.locale())
                .consent(createConsent(
                        requestDto.consentVersion(),
                        requestDto.consentPresenter(),
                        requestDto.consentTimestamp()))
                .build();
    }

    /**
     * Maps management request DTO to consumer information model.
     */
    default ConsumerInformation createConsumerInformation(ManageConsumerInformationRequestDto requestDto) {
        if (requestDto == null) return null;

        return ConsumerInformation.builder()
                .externalConsumerID(requestDto.consumerId())
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .middleName(requestDto.middleName())
                .countryCode(requestDto.countryCode())
                .emails(requestDto.emails())
                .phones(requestDto.phones())
                .locale(requestDto.locale())
                .consent(createConsent(
                        requestDto.consentVersion(),
                        requestDto.consentPresenter(),
                        requestDto.consentTimestamp()))
                .build();
    }

    /**
     * Maps consumer information to consumer info DTO.
     */
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "middleName", source = "middleName")
    @Mapping(target = "countryCode", source = "countryCode")
    @Mapping(target = "emails", source = "emails")
    @Mapping(target = "phones", source = "phones")
    @Mapping(target = "locale", source = "locale")
    ConsumerDataResponseDto.ConsumerInfoDto mapToConsumerInfo(ConsumerInformation consumerInfo);

    // ===============================================
    // Payment Instrument Mappings
    // ===============================================

    /**
     * Creates a card payment instrument from enrollment request DTO.
     */
    default CardPaymentInstrument createCardPaymentInstrument(EnrollDataRequestDto requestDto) {
        if (requestDto == null || !"CARD".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        return CardPaymentInstrument.builder()
                .type("CARD")
                .accountNumber(requestDto.cardNumber())
                .cardType(requestDto.cardType())
                .nameOnCard(requestDto.nameOnCard())
                .expirationDate(requestDto.expirationDate())
                .issuerName(requestDto.issuerName())
                .billingAddress(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a card payment instrument from payment enrollment request DTO.
     */
    default CardPaymentInstrument createCardPaymentInstrument(EnrollPaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null || !"CARD".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        return CardPaymentInstrument.builder()
                .type("CARD")
                .accountNumber(requestDto.cardNumber())
                .cardType(requestDto.cardType())
                .nameOnCard(requestDto.nameOnCard())
                .expirationDate(requestDto.expirationDate())
                .issuerName(requestDto.issuerName())
                .billingAddress(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a card payment instrument from payment management request DTO.
     */
    default CardPaymentInstrument createCardPaymentInstrument(ManagePaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null || !"CARD".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        return CardPaymentInstrument.builder()
                .type("CARD")
                .accountNumber(requestDto.cardNumber())
                .cardType(requestDto.cardType())
                .nameOnCard(requestDto.nameOnCard())
                .expirationDate(requestDto.expirationDate())
                .issuerName(requestDto.issuerName())
                .billingAddress(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a bank account payment instrument from enrollment request DTO.
     */
    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(EnrollDataRequestDto requestDto) {
        if (requestDto == null || !"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        return BankAccountPaymentInstrument.builder()
                .type("BANK_ACCOUNT")
                .accountNumber(requestDto.accountNumber())
                .accountName(requestDto.accountName())
                .accountType(requestDto.accountType())
                .bankName(requestDto.bankName())
                .bankCode(requestDto.bankCode())
                .branchCode(requestDto.branchCode())
                .bankCodeType(requestDto.bankCodeType())
                .currencyCode(requestDto.currencyCode())
                .accountNumberType(requestDto.accountNumberType())
                .bankIdentifierCode(requestDto.bankIdentifierCode())
                .countryCode(requestDto.countryCode())
                .address(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a bank account payment instrument from payment enrollment request DTO.
     */
    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(EnrollPaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null || !"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        // Extract country code from billing address if available
        String countryCode = null;
        if (requestDto.billingAddress() != null) {
            countryCode = requestDto.billingAddress().country();
        }

        return BankAccountPaymentInstrument.builder()
                .type("BANK_ACCOUNT")
                .accountNumber(requestDto.accountNumber())
                .accountName(requestDto.accountName())
                .accountType(requestDto.accountType())
                .bankName(requestDto.bankName())
                .bankCode(requestDto.bankCode())
                .branchCode(requestDto.branchCode())
                .bankCodeType(requestDto.bankCodeType())
                .currencyCode(requestDto.currencyCode())
                .accountNumberType(requestDto.accountNumberType())
                .bankIdentifierCode(requestDto.bankIdentifierCode())
                .countryCode(countryCode)
                .address(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a bank account payment instrument from payment management request DTO.
     */
    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(ManagePaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null || !"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        // Extract country code from billing address if available
        String countryCode = null;
        if (requestDto.billingAddress() != null) {
            countryCode = requestDto.billingAddress().country();
        }

        return BankAccountPaymentInstrument.builder()
                .type("BANK_ACCOUNT")
                .accountNumber(requestDto.accountNumber())
                .accountName(requestDto.accountName())
                .accountType(requestDto.accountType())
                .bankName(requestDto.bankName())
                .bankCode(requestDto.bankCode())
                .branchCode(requestDto.branchCode())
                .bankCodeType(requestDto.bankCodeType())
                .currencyCode(requestDto.currencyCode())
                .accountNumberType(requestDto.accountNumberType())
                .bankIdentifierCode(requestDto.bankIdentifierCode())
                .countryCode(countryCode)
                .address(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    /**
     * Creates a payment instrument list from enrollment request DTO.
     */
    default List<?> createPaymentInstrumentList(EnrollDataRequestDto requestDto) {
        if (requestDto == null) return Collections.emptyList();

        List<Object> instruments = new ArrayList<>();

        if ("CARD".equalsIgnoreCase(requestDto.paymentType())) {
            CardPaymentInstrument card = createCardPaymentInstrument(requestDto);
            if (card != null) {
                instruments.add(card);
            }
        } else if ("BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            BankAccountPaymentInstrument bank = createBankAccountPaymentInstrument(requestDto);
            if (bank != null) {
                instruments.add(bank);
            }
        }

        return instruments;
    }

    /**
     * Creates a payment instrument list from payment enrollment request DTO.
     */
    default List<?> createPaymentInstrumentList(EnrollPaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null) return Collections.emptyList();

        List<Object> instruments = new ArrayList<>();

        if ("CARD".equalsIgnoreCase(requestDto.paymentType())) {
            CardPaymentInstrument card = createCardPaymentInstrument(requestDto);
            if (card != null) {
                instruments.add(card);
            }
        } else if ("BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            BankAccountPaymentInstrument bank = createBankAccountPaymentInstrument(requestDto);
            if (bank != null) {
                instruments.add(bank);
            }
        }

        return instruments;
    }

    /**
     * Creates a payment instrument list from payment management request DTO.
     */
    default List<?> createPaymentInstrumentList(ManagePaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null) return Collections.emptyList();

        List<Object> instruments = new ArrayList<>();

        if ("CARD".equalsIgnoreCase(requestDto.paymentType())) {
            CardPaymentInstrument card = createCardPaymentInstrument(requestDto);
            if (card != null) {
                instruments.add(card);
            }
        } else if ("BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            BankAccountPaymentInstrument bank = createBankAccountPaymentInstrument(requestDto);
            if (bank != null) {
                instruments.add(bank);
            }
        }

        return instruments;
    }

    /**
     * Maps payment instrument to DTO.
     */
    default ConsumerDataResponseDto.PaymentInstrumentInfoDto mapPaymentInstrument(Object instrument) {
        if (instrument == null) return null;

        if (instrument instanceof CardPaymentInstrument ||
                (instrument instanceof java.util.Map &&
                        ((java.util.Map<String, Object>) instrument).containsKey("nameOnCard"))) {
            return mapCardPaymentInstrument(instrument);
        } else {
            return mapBankPaymentInstrument(instrument);
        }
    }

    /**
     * Maps card payment instrument to DTO.
     */
    @SuppressWarnings("unchecked")
    default ConsumerDataResponseDto.PaymentInstrumentInfoDto mapCardPaymentInstrument(Object card) {
        try {
            if (card instanceof java.util.Map) {
                java.util.Map<String, Object> cardMap = (java.util.Map<String, Object>) card;
                return ConsumerDataResponseDto.PaymentInstrumentInfoDto.builder()
                        .type("CARD")
                        .status(getMapString(cardMap, "status"))
                        .accountNumber(getMapString(cardMap, "accountNumber"))
                        .nameOnCard(getMapString(cardMap, "nameOnCard"))
                        .cardType(getMapString(cardMap, "cardType"))
                        .issuerName(getMapString(cardMap, "issuerName"))
                        .expirationDate(getMapString(cardMap, "expirationDate"))
                        .build();
            } else if (card instanceof CardPaymentInstrument cardObj) {
                return ConsumerDataResponseDto.PaymentInstrumentInfoDto.builder()
                        .type("CARD")
                        .accountNumber(cardObj.accountNumber())
                        .nameOnCard(cardObj.nameOnCard())
                        .cardType(cardObj.cardType())
                        .issuerName(cardObj.issuerName())
                        .expirationDate(cardObj.expirationDate())
                        .build();
            }
        } catch (Exception e) {
            // Log error in a real implementation
        }
        return null;
    }

    /**
     * Maps bank account payment instrument to DTO.
     */
    @SuppressWarnings("unchecked")
    default ConsumerDataResponseDto.PaymentInstrumentInfoDto mapBankPaymentInstrument(Object bank) {
        try {
            if (bank instanceof java.util.Map) {
                java.util.Map<String, Object> bankMap = (java.util.Map<String, Object>) bank;
                return ConsumerDataResponseDto.PaymentInstrumentInfoDto.builder()
                        .type("BANK_ACCOUNT")
                        .status(getMapString(bankMap, "status"))
                        .accountNumber(getMapString(bankMap, "accountNumber"))
                        .accountName(getMapString(bankMap, "accountName"))
                        .accountType(getMapString(bankMap, "accountType"))
                        .bankName(getMapString(bankMap, "bankName"))
                        .currencyCode(getMapString(bankMap, "currencyCode"))
                        .build();
            } else if (bank instanceof BankAccountPaymentInstrument bankObj) {
                return ConsumerDataResponseDto.PaymentInstrumentInfoDto.builder()
                        .type("BANK_ACCOUNT")
                        .accountNumber(bankObj.accountNumber())
                        .accountName(bankObj.accountName())
                        .accountType(bankObj.accountType())
                        .bankName(bankObj.bankName())
                        .currencyCode(bankObj.currencyCode())
                        .build();
            }
        } catch (Exception e) {
            // Log error in a real implementation
        }
        return null;
    }

    /**
     * Maps all payment instruments from a GetDataResponse.
     */
    default List<ConsumerDataResponseDto.PaymentInstrumentInfoDto> mapPaymentInstruments(GetDataResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()
                || response.data().getFirst().paymentInstruments() == null) {
            return Collections.emptyList();
        }

        return response.data().getFirst().paymentInstruments().stream()
                .map(this::mapPaymentInstrument)
                .toList();
    }

    /**
     * Creates a delete payment instrument request object.
     */
    default Object createDeletePaymentInstrument(DeletePaymentInstrumentsRequestDto requestDto) {
        if (requestDto == null) return null;

        if ("CARD".equalsIgnoreCase(requestDto.paymentType())) {
            return DeleteCardPaymentInstrument.builder()
                    .type("CARD")
                    .accountNumber(requestDto.cardNumber())
                    .build();
        } else if ("BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return DeleteBankAccountPaymentInstrument.builder()
                    .type("BANK_ACCOUNT")
                    .accountNumber(requestDto.accountNumber())
                    .accountName(requestDto.accountName())
                    .accountNumberType(requestDto.accountNumberType())
                    .build();
        }
        return null;
    }

    // ===============================================
    // Request Mappings (DTO to Model)
    // ===============================================

    /**
     * Maps enrollment request DTO to enrollment request model.
     */
    default EnrollDataRequest mapToEnrollDataRequest(EnrollDataRequestDto dto) {
        if (dto == null) return null;

        return EnrollDataRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerInformation(dto))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    /**
     * Maps payment enrollment request DTO to payment enrollment request model.
     */
    default EnrollPaymentInstrumentsRequest mapToEnrollPaymentInstrumentsRequest(EnrollPaymentInstrumentsRequestDto dto) {
        if (dto == null) return null;

        return EnrollPaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    /**
     * Maps data retrieval request DTO to data retrieval request model.
     */
    default GetDataRequest mapToGetDataRequest(GetDataRequestDto dto) {
        if (dto == null) return null;

        return GetDataRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .build();
    }

    /**
     * Maps consumer management request DTO to consumer management request model.
     */
    default ManageConsumerInformationRequest mapToManageConsumerInformationRequest(ManageConsumerInformationRequestDto dto) {
        if (dto == null) return null;

        return ManageConsumerInformationRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerInformation(dto))
                .build();
    }

    /**
     * Maps payment management request DTO to payment management request model.
     */
    default ManagePaymentInstrumentsRequest mapToManagePaymentInstrumentsRequest(ManagePaymentInstrumentsRequestDto dto) {
        if (dto == null) return null;

        return ManagePaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    /**
     * Maps consumer deletion request DTO to consumer deletion request model.
     */
    default DeleteConsumerInformationRequest mapToDeleteConsumerInformationRequest(DeleteConsumerInformationRequestDto dto) {
        if (dto == null) return null;

        return DeleteConsumerInformationRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .build();
    }

    /**
     * Maps payment deletion request DTO to payment deletion request model.
     */
    default DeletePaymentInstrumentsRequest mapToDeletePaymentInstrumentsRequest(DeletePaymentInstrumentsRequestDto dto) {
        if (dto == null) return null;

        return DeletePaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createDeletePaymentInstrument(dto))
                .build();
    }

    // ===============================================
    // Response Mappings (Model to DTO)
    // ===============================================

    /**
     * Maps enrollment response to enrollment response DTO.
     */
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToEnrollmentResponse(RequestIdResponse response);

    /**
     * Maps management response to management response DTO.
     */
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToManagementResponse(RequestIdResponse response);

    /**
     * Maps deletion response to deletion response DTO.
     */
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToDeletionResponse(RequestIdResponse response);

    /**
     * Maps request status response to request status response DTO.
     */
    @Mapping(target = "status", source = "status")
    @Mapping(target = "consumerId", source = "consumerInformation.externalConsumerID")
    @Mapping(target = "error", ignore = true)
    RequestStatusResponseDto mapToRequestStatusResponse(RequestStatusResponse response);

    /**
     * Maps status detail to status detail DTO.
     */
    @Mapping(target = "intentType", source = "intent.type")
    @Mapping(target = "intentValue", source = "intent.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errors", source = "errorDetails")
    RequestStatusResponseDto.StatusDetailDto mapToStatusDetail(
            RequestStatusResponse.StatusDetail detail);

    /**
     * Maps error detail to error detail DTO for status responses.
     */
    @Mapping(target = "field", source = "field")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", source = "message")
    RequestStatusResponseDto.StatusDetailDto.ErrorDetailDto mapToErrorDetail(
            RequestStatusResponse.ErrorDetail detail);

    /**
     * Maps get data response to consumer data response DTO.
     */
    default ConsumerDataResponseDto mapToConsumerDataResponse(GetDataResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            return ConsumerDataResponseDto.builder()
                    .status("SUCCESS")
                    .build();
        }

        GetDataResponse.DataItem firstItem = response.data().get(0);

        return ConsumerDataResponseDto.builder()
                .status("SUCCESS")
                .consumerId(firstItem.consumerInformation().externalConsumerID())
                .consumerInfo(mapToConsumerInfo(firstItem.consumerInformation()))
                .paymentInstruments(mapPaymentInstruments(response))
                .build();
    }

    // ===============================================
    // Additional Record Types
    // ===============================================

    /**
     * Record for deleting card payment instruments.
     */
    @lombok.Builder
    record DeleteCardPaymentInstrument(
            String type,
            String accountNumber
    ) {
    }

    /**
     * Record for deleting bank account payment instruments.
     */
    @lombok.Builder
    record DeleteBankAccountPaymentInstrument(
            String type,
            String accountName,
            String accountNumber,
            String accountNumberType
    ) {
    }
}