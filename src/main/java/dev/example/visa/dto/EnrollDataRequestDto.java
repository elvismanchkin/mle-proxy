package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record EnrollDataRequestDto(
        // Intent fields
        String intentType,
        String intentValue,

        // Consumer fields
        String consumerId,
        String firstName,
        String lastName,
        String middleName,
        String countryCode,
        List<String> emails,
        List<String> phones,
        String locale,

        // Consent fields
        String consentVersion,
        String consentPresenter,
        String consentTimestamp,

        // Payment instrument type (CARD or BANK_ACCOUNT)
        String paymentType,

        // Card fields
        String cardNumber,
        String cardType,
        String nameOnCard,
        String expirationDate,
        String issuerName,

        // Bank account fields
        String accountNumber,
        String accountName,
        String accountType,
        String bankName,
        String bankCode,
        String branchCode,
        String bankCodeType,
        String currencyCode,
        String accountNumberType,
        String bankIdentifierCode,

        // Address fields
        AddressDto billingAddress
) {
}