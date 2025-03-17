package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ErrorResponse(
        String reason,
        List<ErrorDetail> details,
        String message
) {
    @Serdeable
    @Introspected
    @Builder
    public record ErrorDetail(
            String reason,
            String location
    ) {}
}
