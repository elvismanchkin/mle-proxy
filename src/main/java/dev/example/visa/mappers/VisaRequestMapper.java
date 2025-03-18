package dev.example.visa.mappers;

import dev.example.visa.dto.*;
import dev.example.visa.model.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "jsr330",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VisaRequestMapper {

    // Address mapping
    Address mapToAddress(AddressDto addressDto);

    // Intent mapping
    default Intent createIntent(String type, String value) {
        if (type == null || value == null) {
            return null;
        }
        return Intent.builder()
                .type(type)
                .value(value)
                .build();
    }

    // Consent mapping
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

    // ConsumerInformationIdRef mapping
    default ConsumerInformationIdRef createConsumerIdRef(String externalConsumerId) {
        if (externalConsumerId == null) {
            return null;
        }
        return ConsumerInformationIdRef.builder()
                .externalConsumerID(externalConsumerId)
                .build();
    }

    // ConsumerInformation mapping
    default ConsumerInformation createConsumerInformation(EnrollDataRequestDto requestDto) {
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

    // ConsumerInformation mapping for management
    default ConsumerInformation createConsumerInformation(ManageConsumerInformationRequestDto requestDto) {
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

    // Card Payment Instrument mapping
    default CardPaymentInstrument createCardPaymentInstrument(EnrollDataRequestDto requestDto) {
        if (!"CARD".equalsIgnoreCase(requestDto.paymentType())) {
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

    default CardPaymentInstrument createCardPaymentInstrument(EnrollPaymentInstrumentsRequestDto requestDto) {
        if (!"CARD".equalsIgnoreCase(requestDto.paymentType())) {
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

    default CardPaymentInstrument createCardPaymentInstrument(ManagePaymentInstrumentsRequestDto requestDto) {
        if (!"CARD".equalsIgnoreCase(requestDto.paymentType())) {
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

    // Bank Account Payment Instrument mapping
    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(EnrollDataRequestDto requestDto) {
        if (!"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
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

    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(EnrollPaymentInstrumentsRequestDto requestDto) {
        if (!"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        // We don't have countryCode in EnrollPaymentInstrumentsRequestDto
        // Use a default or get it from billingAddress if available
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
                .countryCode(countryCode) // Use extracted country code
                .address(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    default BankAccountPaymentInstrument createBankAccountPaymentInstrument(ManagePaymentInstrumentsRequestDto requestDto) {
        if (!"BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            return null;
        }

        // We don't have countryCode in ManagePaymentInstrumentsRequestDto
        // Use a default or get it from billingAddress if available
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
                .countryCode(countryCode) // Use extracted country code
                .address(mapToAddress(requestDto.billingAddress()))
                .build();
    }

    // PaymentInstrumentList creation
    default List<?> createPaymentInstrumentList(EnrollDataRequestDto requestDto) {
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

    default List<?> createPaymentInstrumentList(EnrollPaymentInstrumentsRequestDto requestDto) {
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

    default List<?> createPaymentInstrumentList(ManagePaymentInstrumentsRequestDto requestDto) {
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

    // Delete payment instrument object
    default Object createDeletePaymentInstrument(DeletePaymentInstrumentsRequestDto requestDto) {
        if ("CARD".equalsIgnoreCase(requestDto.paymentType())) {
            // Create a Map representation of card deletion data
            Map<String, Object> card = new HashMap<>();
            card.put("type", "CARD");
            card.put("accountNumber", requestDto.cardNumber());
            return card;
        } else if ("BANK_ACCOUNT".equalsIgnoreCase(requestDto.paymentType())) {
            // Create a Map representation of bank account deletion data
            Map<String, Object> bank = new HashMap<>();
            bank.put("type", "BANK_ACCOUNT");
            bank.put("accountNumber", requestDto.accountNumber());
            bank.put("accountName", requestDto.accountName());
            bank.put("accountNumberType", requestDto.accountNumberType());
            return bank;
        }
        return null;
    }

    // Complete request mappings
    default EnrollDataRequest mapToEnrollDataRequest(EnrollDataRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return EnrollDataRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerInformation(dto))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    default EnrollPaymentInstrumentsRequest mapToEnrollPaymentInstrumentsRequest(EnrollPaymentInstrumentsRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return EnrollPaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    default GetDataRequest mapToGetDataRequest(GetDataRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return GetDataRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .build();
    }

    default ManageConsumerInformationRequest mapToManageConsumerInformationRequest(ManageConsumerInformationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return ManageConsumerInformationRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerInformation(dto))
                .build();
    }

    default ManagePaymentInstrumentsRequest mapToManagePaymentInstrumentsRequest(ManagePaymentInstrumentsRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return ManagePaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createPaymentInstrumentList(dto))
                .build();
    }

    default DeleteConsumerInformationRequest mapToDeleteConsumerInformationRequest(DeleteConsumerInformationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return DeleteConsumerInformationRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .build();
    }

    default DeletePaymentInstrumentsRequest mapToDeletePaymentInstrumentsRequest(DeletePaymentInstrumentsRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return DeletePaymentInstrumentsRequest.builder()
                .intent(createIntent(dto.intentType(), dto.intentValue()))
                .consumerInformation(createConsumerIdRef(dto.consumerId()))
                .paymentInstruments(createDeletePaymentInstrument(dto))
                .build();
    }
}