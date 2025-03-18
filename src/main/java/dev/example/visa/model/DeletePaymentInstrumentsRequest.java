package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record DeletePaymentInstrumentsRequest(
        Intent intent,
        Object paymentInstruments,
        ConsumerInformationIdRef consumerInformation
) {
}
