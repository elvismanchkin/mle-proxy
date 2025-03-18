package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record BankAccountPaymentInstrument(
        String type,
        Address address,
        String bankCode,
        String bankName,
        String branchCode,
        String accountName,
        String accountType,
        String countryCode,
        String bankCodeType,
        String currencyCode,
        String accountNumber,
        String accountNumberType,
        String bankIdentifierCode) {}
