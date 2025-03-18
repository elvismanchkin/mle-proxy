package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record DeletePaymentInstrumentsRequestDto(
        // Intent fields
        String intentType,
        String intentValue,

        // Consumer ID
        String consumerId,

        // Payment instrument type (CARD or BANK_ACCOUNT)
        String paymentType,

        // Card fields (for deletion identification)
        String cardNumber,

        // Bank account fields (for deletion identification)
        String accountNumber,
        String accountName,
        String accountNumberType
) {
}