package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record CardPaymentInstrument(
        String type,
        String cardType,
        String issuerName,
        String nameOnCard,
        String accountNumber,
        Address billingAddress,
        String expirationDate
) {
}
