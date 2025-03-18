package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ManagePaymentInstrumentsRequest(
        Intent intent,
        List<?> paymentInstruments,
        ConsumerInformationIdRef consumerInformation
) {
}
