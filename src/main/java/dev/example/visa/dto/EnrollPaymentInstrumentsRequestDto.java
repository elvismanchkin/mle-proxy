package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record EnrollPaymentInstrumentsRequestDto(
        // Intent fields
        String intentType,
        String intentValue,

        // Consumer ID
        String consumerId,

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