package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record RequestStatusResponse(
        String status,
        List<StatusDetail> details,
        ConsumerInformationIdRef consumerInformation
) {
    @Serdeable
    @Introspected
    @Builder
    public record StatusDetail(
            Intent intent,
            String status,
            List<ErrorDetail> errorDetails
    ) {
    }

    @Serdeable
    @Introspected
    @Builder
    public record ErrorDetail(
            String field,
            String reason,
            String message
    ) {
    }
}
