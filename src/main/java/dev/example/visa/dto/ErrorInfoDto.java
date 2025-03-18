package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ErrorInfoDto(
        String reason,
        String message,
        List<ErrorDetailDto> details
) {
    @Serdeable
    @Introspected
    @Builder
    public record ErrorDetailDto(
            String field,
            String reason,
            String message
    ) {
    }
}