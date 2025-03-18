package dev.example.visa.mappers;

import dev.example.visa.dto.ConsumerDataResponseDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.ErrorInfoDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.model.BankAccountPaymentInstrument;
import dev.example.visa.model.CardPaymentInstrument;
import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ErrorResponse;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.RequestIdResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "jsr330",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VisaResponseMapper {

    // Error mapping
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "details", source = "details")
    ErrorInfoDto mapToErrorInfo(ErrorResponse errorResponse);

    @Mapping(target = "field", source = "location")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", ignore = true)
    ErrorInfoDto.ErrorDetailDto mapToErrorDetail(ErrorResponse.ErrorDetail detail);

    // Enrollment responses (enrollData, enrollPaymentInstruments)
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToEnrollmentResponse(RequestIdResponse response);

    // Request Status response
    @Mapping(target = "status", source = "status")
    @Mapping(target = "consumerId", source = "consumerInformation.externalConsumerID")
    @Mapping(target = "error", ignore = true)
    RequestStatusResponseDto mapToRequestStatusResponse(dev.example.visa.model.RequestStatusResponse response);

    @Mapping(target = "intentType", source = "intent.type")
    @Mapping(target = "intentValue", source = "intent.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errors", source = "errorDetails")
    RequestStatusResponseDto.StatusDetailDto mapToStatusDetail(
            dev.example.visa.model.RequestStatusResponse.StatusDetail detail);

    @Mapping(target = "field", source = "field")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "message", source = "message")
    RequestStatusResponseDto.StatusDetailDto.ErrorDetailDto mapToErrorDetail(
            dev.example.visa.model.RequestStatusResponse.ErrorDetail detail);

    // GetData response
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

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "middleName", source = "middleName")
    @Mapping(target = "countryCode", source = "countryCode")
    @Mapping(target = "emails", source = "emails")
    @Mapping(target = "phones", source = "phones")
    @Mapping(target = "locale", source = "locale")
    ConsumerDataResponseDto.ConsumerInfoDto mapToConsumerInfo(ConsumerInformation consumerInfo);

    // Management responses (managePaymentInstruments, manageConsumerInformation)
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToManagementResponse(RequestIdResponse response);

    // Deletion responses (deleteConsumerInformation, deletePaymentInstruments)
    @Mapping(target = "status", constant = "SUCCESS")
    @Mapping(target = "requestId", source = "requestTraceId")
    @Mapping(target = "error", ignore = true)
    EnrollmentResponseDto mapToDeletionResponse(RequestIdResponse response);

    // Helper methods for payment instruments
    default List<ConsumerDataResponseDto.PaymentInstrumentInfoDto> mapPaymentInstruments(GetDataResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()
                || response.data().getFirst().paymentInstruments() == null) {
            return Collections.emptyList();
        }

        return response.data().getFirst().paymentInstruments().stream()
                .map(this::mapPaymentInstrument)
                .toList();
    }

    @SuppressWarnings("unchecked")
    default ConsumerDataResponseDto.PaymentInstrumentInfoDto mapPaymentInstrument(Object instrument) {
        if (instrument == null) return null;

        // We need to parse based on type - this example assumes the types match the actual structure
        // In a real implementation, we'd need to check for map attributes and class instance structure
        if (instrument instanceof CardPaymentInstrument ||
                (instrument instanceof java.util.Map && ((java.util.Map<String, Object>) instrument).containsKey("nameOnCard"))) {
            return mapCardPaymentInstrument(instrument);
        } else {
            return mapBankPaymentInstrument(instrument);
        }
    }

    @SuppressWarnings("unchecked")
    default ConsumerDataResponseDto.PaymentInstrumentInfoDto mapCardPaymentInstrument(Object card) {
        // This is a simplified example - in the real world you'd need to handle the actual JSON structure
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

    // Helper for safely getting string values from maps
    default String getMapString(java.util.Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    // Error response creation methods
    default EnrollmentResponseDto createErrorEnrollmentResponse(ErrorResponse errorResponse) {
        return dev.example.visa.dto.EnrollmentResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }

    default RequestStatusResponseDto createErrorStatusResponse(ErrorResponse errorResponse) {
        return RequestStatusResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }

    default ConsumerDataResponseDto createErrorDataResponse(ErrorResponse errorResponse) {
        return ConsumerDataResponseDto.builder()
                .status("ERROR")
                .error(mapToErrorInfo(errorResponse))
                .build();
    }
}