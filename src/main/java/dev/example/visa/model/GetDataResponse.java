package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record GetDataResponse(
        List<DataItem> data
) {
    @Serdeable
    @Introspected
    @Builder
    public record DataItem(
            Intent intent,
            List<?> paymentInstruments,
            ConsumerInformation consumerInformation
    ) {
    }
}
